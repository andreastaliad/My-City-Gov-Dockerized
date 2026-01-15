package gr.hua.dit.my.city.gov.core.service;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

//check every minute for appointments in the next hour and send reminders
@Service
public class AppointmentReminderService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderService.class);

    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public AppointmentReminderService(AppointmentRepository appointmentRepository,
                                      PersonRepository personRepository,
                                      SmsSender smsSender,
                                      EmailSender emailSender) {
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    // if not already send and in within the next hour, send reminder
    @Scheduled(cron = "0 * * * * *")
    public void sendUpcomingAppointmentReminders() {
        // Correct Time Zone for reminders
        ZoneId zone = ZoneId.of("Europe/Athens");
        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone).withSecond(0).withNano(0);

        List<Appointment> allAppointments = appointmentRepository.findAll();

        for (Appointment appointment : allAppointments) {
            if (appointment == null || appointment.getDate() == null || appointment.getTime() == null) {
                continue;
            }
            if (appointment.isReminderSent()) {
                continue; // already reminded
            }

            if (!today.equals(appointment.getDate())) {
                continue;
            }

            LocalTime apptTime = appointment.getTime().withSecond(0).withNano(0);

            long minutesUntil = ChronoUnit.MINUTES.between(now, apptTime);

                logger.debug("Reminder check for appointment {}: today={}, apptDate={}, apptTime={}, now={}, minutesUntil={}, alreadySent={}",
                    appointment.getId(), today, appointment.getDate(), apptTime, now, minutesUntil, appointment.isReminderSent());

            
            if (minutesUntil >= 0 && minutesUntil < 60) {
                Long personId = appointment.getPersonId();
                if (personId == null) {
                    logger.debug("Skipping reminder for appointment {}: no personId", appointment.getId());
                    continue;
                }

                Person person = personRepository.findById(personId).orElse(null);
                if (person == null) {
                    logger.debug("Skipping reminder for appointment {}: person not found", appointment.getId());
                    continue;
                }

                String message = String.format(
                        "Your appointment for %s is today at %s.",
                        appointment.getService() != null ? appointment.getService() : "My City Gov",
                        appointment.getTime().toString()
                );

                String phone = person.getPhoneNumber();
                if (phone != null && !phone.isBlank()) {
                    boolean sent = smsSender.sendSms(phone, message);
                    if (sent) {
                        logger.info("Sent appointment reminder SMS for appointment {} to {}", appointment.getId(), phone);
                    } else {
                        logger.warn("Failed to send appointment reminder SMS for appointment {} to {}", appointment.getId(), phone);
                    }
                }

                String email = person.getEmailAddress();
                if (email != null && !email.isBlank()) {
                    emailSender.sendSimpleEmail(
                            email,
                            "Appointment reminder",
                            message
                    );
                }

                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);
                logger.info("Marked appointment {} as reminded", appointment.getId());
            }
        }
    }
}
