package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import gr.hua.dit.my.city.gov.core.model.ServiceUnitSchedule;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitRepository;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.Arrays;

@Controller
@RequestMapping("/admin/service-units")
public class AdminServiceUnitScheduleController {

    private final ServiceUnitRepository serviceUnitRepository;
    private final ServiceUnitScheduleRepository scheduleRepository;

    public AdminServiceUnitScheduleController(ServiceUnitRepository serviceUnitRepository,
                                              ServiceUnitScheduleRepository scheduleRepository) {
        this.serviceUnitRepository = serviceUnitRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // fragment/tab: λίστα + form για ένα συγκεκριμένο serviceUnit
    @GetMapping("/{id}/schedules")
    public String schedules(@PathVariable Long id, Model model) {
        ServiceUnit su = serviceUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ServiceUnit not found"));

        model.addAttribute("serviceUnit", su);
        model.addAttribute("schedules", scheduleRepository.findByServiceUnitIdOrderByDayOfWeekAscStartTimeAsc(id));
        model.addAttribute("days", Arrays.asList(DayOfWeek.values()));
        model.addAttribute("scheduleForm", new ServiceUnitSchedule());
        return "admin/service_unit_schedule :: content";
    }

    @PostMapping("/{id}/schedules")
    public String createSchedule(@PathVariable Long id,
                                 @ModelAttribute("scheduleForm") ServiceUnitSchedule form,
                                 RedirectAttributes ra) {

        ServiceUnit su = serviceUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ServiceUnit not found"));

        // βασικοί έλεγχοι
        if (form.getStartTime() == null || form.getEndTime() == null || form.getDayOfWeek() == null) {
            ra.addFlashAttribute("error", "Συμπλήρωσε ημέρα και ώρες.");
            return "redirect:/admin/service-units/" + id + "/schedules";
        }
        if (!form.getEndTime().isAfter(form.getStartTime())) {
            ra.addFlashAttribute("error", "Η ώρα λήξης πρέπει να είναι μετά την ώρα έναρξης.");
            return "redirect:/admin/service-units/" + id + "/schedules";
        }
        if (form.getSlotMinutes() <= 0 || form.getSlotMinutes() > 180) {
            ra.addFlashAttribute("error", "Slot minutes μη έγκυρο.");
            return "redirect:/admin/service-units/" + id + "/schedules";
        }

        form.setServiceUnit(su);
        scheduleRepository.save(form);
        ra.addFlashAttribute("success", "Το ωράριο αποθηκεύτηκε.");
        return "redirect:/admin/service-units/" + id + "/schedules";
    }

    @PostMapping("/{serviceUnitId}/schedules/{scheduleId}/toggle")
    public String toggle(@PathVariable Long serviceUnitId,
                         @PathVariable Long scheduleId) {
        ServiceUnitSchedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        s.setActive(!s.isActive());
        scheduleRepository.save(s);
        return "redirect:/admin/service-units/" + serviceUnitId + "/schedules";
    }

    @PostMapping("/{serviceUnitId}/schedules/{scheduleId}/delete")
    public String delete(@PathVariable Long serviceUnitId,
                         @PathVariable Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
        return "redirect:/admin/service-units/" + serviceUnitId + "/schedules";
    }
}
