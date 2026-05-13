import React from "react";
import { useEffect, useState } from "react";
import Message from "../components/Message.jsx";
import api from "../services/api.js";

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [venues, setVenues] = useState([]);
  const [pendingEvents, setPendingEvents] = useState([]);
  const [allEvents, setAllEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("pending");
  const [venueName, setVenueName] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setError("");
    try {
      const [usersResponse, pendingResponse, eventsResponse] = await Promise.all([
        api.get("/admin/users"),
        api.get("/events/admin/pending"),
        api.get("/events/admin/all"),
      ]);
      const venuesResponse = await api.get("/venues");
      setUsers(usersResponse.data);
      setVenues(venuesResponse.data);
      setPendingEvents(pendingResponse.data);
      setAllEvents(eventsResponse.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load admin dashboard");
    }
  }

  async function updateStatus(eventId, action) {
    setError("");
    setSuccess("");
    try {
      await api.put(`/events/admin/${action}/${eventId}`);
      setSuccess(`Event ${action}d successfully`);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || `Could not ${action} event`);
    }
  }

  async function editEvent(eventItem) {
    const eventName = window.prompt("Event name", eventItem.eventName);
    if (eventName === null) return;
    const eventDate = window.prompt("Event date (YYYY-MM-DD)", eventItem.eventDate);
    if (eventDate === null) return;
    const startTime = window.prompt("Start time (HH:mm)", eventItem.startTime?.slice(0, 5) || "");
    if (startTime === null) return;
    const endTime = window.prompt("End time (HH:mm)", eventItem.endTime?.slice(0, 5) || "");
    if (endTime === null) return;
    const venueOptions = venues.map((v) => `${v.id}: ${v.name}`).join("\n");
    const venueIdText = window.prompt(`Venue ID\n${venueOptions}`, String(eventItem.venueId));
    if (venueIdText === null) return;
    const description = window.prompt("Description", eventItem.description || "");
    if (description === null) return;

    const venueId = Number(venueIdText.trim());
    if (!eventName.trim() || !eventDate.trim() || !startTime.trim() || !endTime.trim() || Number.isNaN(venueId)) {
      setError("Please provide valid event name, date, time, and venue id");
      return;
    }

    setError("");
    setSuccess("");
    try {
      await api.put(`/events/admin/${eventItem.eventId}`, {
        eventName: eventName.trim(),
        eventDate: eventDate.trim(),
        startTime: normalizeTime(startTime),
        endTime: normalizeTime(endTime),
        venueId,
        description: description.trim(),
      });
      setSuccess("Event updated successfully");
      await loadData();
    } catch (err) {
      const status = err.response?.status;
      const details = err.response?.data?.message || err.response?.data?.error;
      setError(details ? `Update failed (${status || "unknown"}): ${details}` : `Could not update event (${status || "unknown"})`);
    }
  }

  async function updateUser(user, values) {
    setError("");
    setSuccess("");
    try {
      await api.put(`/admin/users/${user.id}`, { ...user, ...values });
      setSuccess("User updated successfully");
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update user");
    }
  }

  async function toggleUser(user) {
    setError("");
    setSuccess("");
    try {
      await api.patch(`/admin/users/${user.id}/${user.enabled ? "disable" : "enable"}`);
      setSuccess(`User ${user.enabled ? "disabled" : "enabled"} successfully`);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update user status");
    }
  }

  async function deleteUser(user) {
    setError("");
    setSuccess("");
    try {
      await api.delete(`/admin/users/${user.id}`);
      setSuccess("User deleted successfully");
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not delete user");
    }
  }

  async function createVenue(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post("/venues", { name: venueName });
      setVenueName("");
      setSuccess("Venue created successfully");
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create venue");
    }
  }

  async function renameVenue(venue, name) {
    setError("");
    setSuccess("");
    try {
      await api.put(`/venues/${venue.id}`, { name });
      setSuccess("Venue renamed successfully");
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not rename venue");
    }
  }

  async function deleteVenue(venue) {
    setError("");
    setSuccess("");
    try {
      await api.delete(`/venues/${venue.id}`);
      setSuccess("Venue deleted successfully");
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Could not delete venue");
    }
  }

  return (
    <section className="dashboard-grid">
      <aside className="sidebar">
        <button className={activeTab === "pending" ? "nav-active" : ""} onClick={() => setActiveTab("pending")}>Pending Requests</button>
        <button className={activeTab === "events" ? "nav-active" : ""} onClick={() => setActiveTab("events")}>All Events</button>
        <button className={activeTab === "users" ? "nav-active" : ""} onClick={() => setActiveTab("users")}>Users</button>
        <button className={activeTab === "venues" ? "nav-active" : ""} onClick={() => setActiveTab("venues")}>Venues</button>
      </aside>

      <div className="content-panel">
        <Message error={error} success={success} />

        {activeTab === "pending" && (
          <>
            <div className="section-title">
              <p className="eyebrow">Admin</p>
              <h2>Pending event requests</h2>
            </div>
            <EventTable events={pendingEvents} showActions onUpdateStatus={updateStatus} />
          </>
        )}

        {activeTab === "events" && (
          <>
            <div className="section-title">
              <p className="eyebrow">Admin</p>
              <h2>All events</h2>
            </div>
            <EventTable events={allEvents} showEditActions onEditEvent={editEvent} />
          </>
        )}

        {activeTab === "users" && (
          <>
            <div className="section-title">
              <p className="eyebrow">Admin</p>
              <h2>System users</h2>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <UserRow
                      key={user.id}
                      user={user}
                      onUpdate={updateUser}
                      onToggle={toggleUser}
                      onDelete={deleteUser}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}

        {activeTab === "venues" && (
          <>
            <div className="section-title">
              <p className="eyebrow">Admin</p>
              <h2>Manage venues</h2>
            </div>
            <form className="inline-form" onSubmit={createVenue}>
              <input value={venueName} onChange={(event) => setVenueName(event.target.value)} placeholder="New venue name" required />
              <button className="primary-action" type="submit">Add Venue</button>
            </form>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {venues.map((venue) => (
                    <VenueRow key={venue.id} venue={venue} onRename={renameVenue} onDelete={deleteVenue} />
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </section>
  );
}

function normalizeTime(value) {
  const text = value.trim();
  if (/^\d{2}:\d{2}:\d{2}$/.test(text)) return text;
  if (/^\d{2}:\d{2}$/.test(text)) return `${text}:00`;
  return text;
}

function UserRow({ user, onUpdate, onToggle, onDelete }) {
  const [email, setEmail] = useState(user.email);
  const [role, setRole] = useState(user.role);

  return (
    <tr>
      <td>{user.id}</td>
      <td>{user.username}</td>
      <td><input value={email} onChange={(event) => setEmail(event.target.value)} /></td>
      <td>
        <select value={role} onChange={(event) => setRole(event.target.value)}>
          <option value="ORGANIZER">ORGANIZER</option>
          <option value="ADMIN">ADMIN</option>
        </select>
      </td>
      <td><span className={`status ${user.enabled ? "approved" : "rejected"}`}>{user.enabled ? "ENABLED" : "DISABLED"}</span></td>
      <td>
        <div className="action-row">
          <button type="button" onClick={() => onUpdate(user, { email, role })}>Save</button>
          <button type="button" onClick={() => onToggle(user)}>{user.enabled ? "Disable" : "Enable"}</button>
          <button type="button" className="danger" onClick={() => onDelete(user)}>Delete</button>
        </div>
      </td>
    </tr>
  );
}

function VenueRow({ venue, onRename, onDelete }) {
  const [name, setName] = useState(venue.name);

  return (
    <tr>
      <td>{venue.id}</td>
      <td><input value={name} onChange={(event) => setName(event.target.value)} /></td>
      <td>
        <div className="action-row">
          <button type="button" onClick={() => onRename(venue, name)}>Save</button>
          <button type="button" className="danger" onClick={() => onDelete(venue)}>Delete</button>
        </div>
      </td>
    </tr>
  );
}

function EventTable({ events, showActions = false, onUpdateStatus, showEditActions = false, onEditEvent }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Event</th>
            <th>Date</th>
            <th>Time</th>
            <th>Venue</th>
            <th>Organizer</th>
            <th>Status</th>
            {showActions && <th>Action</th>}
            {showEditActions && <th>Edit</th>}
          </tr>
        </thead>
        <tbody>
          {events.length === 0 && (
            <tr>
              <td colSpan={showActions || showEditActions ? 8 : 6} className="empty-cell">No records found</td>
            </tr>
          )}
          {events.map((event) => (
            <tr key={event.eventId}>
              <td>{event.eventName}</td>
              <td>{event.eventDate}</td>
              <td>{event.startTime} - {event.endTime}</td>
              <td>{event.venueName}</td>
              <td>{event.organizerUsername}</td>
              <td><span className={`status ${event.status.toLowerCase()}`}>{event.status}</span></td>
              {showActions && (
                <td>
                  <div className="action-row">
                    <button type="button" onClick={() => onUpdateStatus(event.eventId, "approve")}>Approve</button>
                    <button type="button" className="danger" onClick={() => onUpdateStatus(event.eventId, "reject")}>Reject</button>
                  </div>
                </td>
              )}
              {showEditActions && (
                <td>
                  <button type="button" onClick={() => onEditEvent(event)}>Edit</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
