package studio.humpback.backend.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import studio.humpback.backend.service.DashboardService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardScheduler {

    private final DashboardService dashboardService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupDashboardSnapshot() {
        refreshDashboardSnapshotSafely();
    }

    @Scheduled(cron = "${dashboard.scheduler.cron:0 */5 * * * *}")
    public void refreshDashboardSnapshot() {
        refreshDashboardSnapshotSafely();
    }

    private void refreshDashboardSnapshotSafely() {
        try {
            dashboardService.refreshAllSnapshots();
            log.debug("Dashboard snapshot refreshed");
        } catch (Exception ex) {
            log.error("Failed to refresh dashboard snapshot", ex);
        }
    }
}
