package com.eventsphere.dto;

import java.util.List;

/**
 * DTO wrapping all three event categories for the dashboard endpoint.
 * Returned by GET /api/events/dashboard.
 */
public class DashboardResponse {

    private List<EventResponse> live;
    private List<EventResponse> upcoming;
    private List<EventResponse> previous;

    public DashboardResponse() {
    }

    public DashboardResponse(List<EventResponse> live, List<EventResponse> upcoming, List<EventResponse> previous) {
        this.live = live;
        this.upcoming = upcoming;
        this.previous = previous;
    }

    public static DashboardResponseBuilder builder() {
        return new DashboardResponseBuilder();
    }

    public static class DashboardResponseBuilder {
        private List<EventResponse> live;
        private List<EventResponse> upcoming;
        private List<EventResponse> previous;

        public DashboardResponseBuilder live(List<EventResponse> live) {
            this.live = live;
            return this;
        }

        public DashboardResponseBuilder upcoming(List<EventResponse> upcoming) {
            this.upcoming = upcoming;
            return this;
        }

        public DashboardResponseBuilder previous(List<EventResponse> previous) {
            this.previous = previous;
            return this;
        }

        public DashboardResponse build() {
            return new DashboardResponse(live, upcoming, previous);
        }
    }

    public List<EventResponse> getLive() {
        return live;
    }

    public void setLive(List<EventResponse> live) {
        this.live = live;
    }

    public List<EventResponse> getUpcoming() {
        return upcoming;
    }

    public void setUpcoming(List<EventResponse> upcoming) {
        this.upcoming = upcoming;
    }

    public List<EventResponse> getPrevious() {
        return previous;
    }

    public void setPrevious(List<EventResponse> previous) {
        this.previous = previous;
    }
}
