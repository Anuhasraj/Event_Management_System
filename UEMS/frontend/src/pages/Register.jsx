import React from "react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Message from "../components/Message.jsx";
import api from "../services/api.js";
import { saveSession } from "../services/auth.js";

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState("");

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");

    try {
      const response = await api.post("/auth/register", form);
      saveSession(response.data);
      navigate(response.data.role === "ADMIN" ? "/admin" : "/organizer");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
    }
  }

  return (
    <section className="auth-layout">
      <div className="auth-panel">
        <div className="section-title">
          <p className="eyebrow">Create account</p>
          <h2>Register for UEMS</h2>
        </div>
        <Message error={error} />
        <form className="form-grid" onSubmit={submit}>
          <label>
            Username
            <input name="username" value={form.username} onChange={updateField} required />
          </label>
          <label>
            Email
            <input name="email" type="email" value={form.email} onChange={updateField} required />
          </label>
          <label>
            Password
            <input name="password" type="password" minLength="6" value={form.password} onChange={updateField} required />
          </label>
          <button className="primary-action" type="submit">Register</button>
        </form>
        <p className="auth-link">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </section>
  );
}
