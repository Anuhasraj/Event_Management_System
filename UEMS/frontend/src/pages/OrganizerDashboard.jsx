import React from "react";
import { useEffect, useState } from "react";
import Message from "../components/Message.jsx";
import api from "../services/api.js";

const initialForm = {
  eventName: "",
  eventDate: "",
  startTime: "",
  endTime: "",
  venueId: "",
  description: "",
};

export default function OrganizerDashboard() {
  const [venues, setVenues] = useState([]);
  const [events, setEvents] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedVenue, setSelectedVenue] = useState("");
  const [availability, setAvailability] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadInitialData();
  }, []);

  async function loadInitialData() {
    setError("");
    try {
      const [venuesResponse, eventsResponse] = await Promise.all([
        api.get("/venues"),
        api.get("/events/my"),
      ]);
      setVenues(venuesResponse.data);
      setEvents(eventsResponse.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load organizer dashboard");
    }
  }

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submitEvent(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post("/events/request", {
        ...form,
        venueId: Number(form.venueId),
      });
      setForm(initialForm);
      setSuccess("Event request sent to admin");
      await loadInitialData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not request event");
    }
  }

  async function checkAvailability(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    setAvailability(null);
    if (!selectedVenue || !selectedDate) {
      setError("Select venue and date first");
      return;
    }
    try {
      const response = await api.get(`/events/venue/${selectedVenue}/availability`, {
        params: { date: selectedDate },
      });
      setAvailability(response.data);
      setSuccess(response.data.availableSlots.length === 0 ? "No free slots for this date" : "Available slots loaded");
    } catch (err) {
      setError(err.response?.data?.message || "Could not check availability");
    }
  }

  return (
    <section className="organizer-grid">
      <div className="content-panel">
        <div className="section-title">
          <p className="eyebrow">Organizer</p>
          <h2>Request new event</h2>
        </div>
        <Message error={error} success={success} />
        <form className="form-grid two-column" onSubmit={submitEvent}>
          <label>
            Event name
            <input name="eventName" value={form.eventName} onChange={updateField} required />
          </label>
          <label>
            Venue
            <select name="venueId" value={form.venueId} onChange={updateField} required>
              <option value="">Select venue</option>
              {venues.map((venue) => (
                <option key={venue.id} value={venue.id}>{venue.name}</option>
              ))}
            </select>
          </label>
          <label>
            Date
            <input name="eventDate" type="date" value={form.eventDate} onChange={updateField} required />
          </label>
          <label>
            Start time
            <input name="startTime" type="time" value={form.startTime} onChange={updateField} required />
          </label>
          <label>
            End time
            <input name="endTime" type="time" value={form.endTime} onChange={updateField} required />
          </label>
          <label className="full-width">
            Description
            <textarea name="description" value={form.description} onChange={updateField} rows="4" />
          </label>
          <button className="primary-action" type="submit">Send Request</button>
        </form>
      </div>

      <div className="content-panel">
        <div className="section-title">
          <p className="eyebrow">Availability</p>
          <h2>Check venue schedule</h2>
        </div>
        <form className="form-grid" onSubmit={checkAvailability}>
          <label>
            Venue
            <select value={selectedVenue} onChange={(event) => setSelectedVenue(event.target.value)}>
              <option value="">Select venue</option>
              {venues.map((venue) => (
                <option key={venue.id} value={venue.id}>{venue.name}</option>
              ))}
            </select>
          </label>
          <label>
            Date
            <input type="date" value={selectedDate} onChange={(event) => setSelectedDate(event.target.value)} />
          </label>
          <button className="secondary-action" type="submit">Check Availability</button>
        </form>
        <div className="schedule-list">
          {availability?.availableSlots.map((slot) => (
            <div className="schedule-item free-slot" key={`${slot.startTime}-${slot.endTime}`}>
              <strong>{slot.startTime} - {slot.endTime}</strong>
              <span>Available</span>
            </div>
          ))}
          {availability?.bookings.map((item) => (
            <div className="schedule-item" key={item.eventId}>
              <strong>{item.startTime} - {item.endTime}</strong>
              <span>{item.eventName}</span>
              <em>{item.status}</em>
            </div>
          ))}
        </div>
      </div>

      <div className="content-panel span-full">
        <div className="section-title">
          <p className="eyebrow">My requests</p>
          <h2>Event status</h2>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Event</th>
                <th>Date</th>
                <th>Time</th>
                <th>Venue</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {events.length === 0 && (
                <tr>
                  <td colSpan="5" className="empty-cell">No requests yet</td>
                </tr>
              )}
              {events.map((event) => (
                <tr key={event.eventId}>
                  <td>{event.eventName}</td>
                  <td>{event.eventDate}</td>
                  <td>{event.startTime} - {event.endTime}</td>
                  <td>{event.venueName}</td>
                  <td><span className={`status ${event.status.toLowerCase()}`}>{event.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
