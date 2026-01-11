package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.model.ServiceUnitSchedule;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitRepository;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitScheduleRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
public class AppointmentsController {

    @Autowired
    private ServiceUnitScheduleRepository scheduleRepository;
    @Autowired
    private ServiceUnitRepository serviceUnitRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @GetMapping("/appointments/form")
    public String showAppointmentForm(Model model) {
        model.addAttribute("serviceUnits", serviceUnitRepository.findByActiveTrueOrderByNameAsc());
        return "appointments-form :: content";
    }


    @GetMapping("/appointments/available-times")
    @ResponseBody
    public List<String> availableTimes(
            @RequestParam Long serviceUnitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // dont allow past dates
        if (date.isBefore(today)) {
            return List.of();
        }

        DayOfWeek dow = date.getDayOfWeek();

        List<ServiceUnitSchedule> schedules =
                scheduleRepository.findByServiceUnitIdAndDayOfWeekAndActiveTrue(serviceUnitId, dow);

        Set<LocalTime> booked = appointmentRepository
                .findByServiceUnitIdAndDate(serviceUnitId, date)
                .stream()
                .map(Appointment::getTime)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        TreeSet<LocalTime> slots = new TreeSet<>();
        for (ServiceUnitSchedule s : schedules) {
            int step = s.getSlotMinutes();
            LocalTime t = s.getStartTime();
            LocalTime lastStart = s.getEndTime().minusMinutes(step);

            while (!t.isAfter(lastStart)) {
                // For today, skip time slots that are already in the past.
                if (date.isEqual(today) && t.isBefore(now)) {
                    t = t.plusMinutes(step);
                    continue;
                }

                if (!booked.contains(t)) slots.add(t);
                t = t.plusMinutes(step);
            }
        }

        if (serviceUnitId == null || date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing params");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return slots.stream().map(fmt::format).toList();
    }

    @GetMapping("/appointments/available-days")
    @ResponseBody
    public List<Integer> availableDays(@RequestParam Long serviceUnitId) {

        // Παίρνουμε όλα τα schedules (ή μόνο active) και κρατάμε τις ημέρες που είναι ενεργές
        return scheduleRepository.findByServiceUnitIdOrderByDayOfWeekAscStartTimeAsc(serviceUnitId)
                .stream()
                .filter(ServiceUnitSchedule::isActive)
                .map(s -> s.getDayOfWeek().getValue()) // 1=Mon ... 7=Sun
                .distinct()
                .sorted()
                .toList();
    }

    @GetMapping("/appointments/available-dates")
    @ResponseBody
    public List<String> availableDates(
            @RequestParam Long serviceUnitId,
            @RequestParam(defaultValue = "21") int daysAhead
    ) {
        if (daysAhead < 1) daysAhead = 1;
        if (daysAhead > 60) daysAhead = 60;

        Set<Integer> allowedDow = scheduleRepository
                .findByServiceUnitIdOrderByDayOfWeekAscStartTimeAsc(serviceUnitId)
                .stream()
                .filter(ServiceUnitSchedule::isActive)
                .map(s -> s.getDayOfWeek().getValue()) // 1=Mon..7=Sun
                .collect(Collectors.toSet());

        if (allowedDow.isEmpty()) return List.of();

        // Allow booking from today (current date) onwards; past dates are
        // filtered out by availableTimes/saveAppointment.
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(daysAhead - 1);

        List<String> out = new java.util.ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (allowedDow.contains(d.getDayOfWeek().getValue())) {
                out.add(d.toString()); // yyyy-MM-dd
            }
        }
        return out;
    }

    @PostMapping("/appointments")
    public String saveAppointment(Appointment appointment) {
        // associate with current user, if logged in
        currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .ifPresent(appointment::setPersonId);

        serviceUnitRepository.findById(appointment.getServiceUnitId())
                .ifPresent(su -> appointment.setService(su.getName()));

        LocalDate today = LocalDate.now();

        // 1) Date not in the past (ή όχι σήμερα)
        if (appointment.getDate() == null || appointment.getDate().isBefore(today)) {
            // γύρνα error fragment ή redirect με μήνυμα
            return "appointments-form :: content";
        }

        // 2) Αν date == today, ώρα να μην είναι στο παρελθόν
        if (appointment.getDate().isEqual(today) && appointment.getTime() != null) {
            LocalTime now = LocalTime.now();
            if (appointment.getTime().isBefore(now)) {
                return "appointments-form :: content";
            }
        }

        //για αποθήκευση στη βάση
        appointmentRepository.save(appointment);
        return "appointments-success :: content";
    }
}
