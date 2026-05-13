import React from "react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Message from "../components/Message.jsx";
import api from "../services/api.js";
import { saveSession } from "../services/auth.js";

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");

    try {
      const response = await api.post("/auth/login", form);
      saveSession(response.data);
      navigate(response.data.role === "ADMIN" ? "/admin" : "/organizer");
    } catch (err) {
      setError(err.response?.data?.message || "Login failed");
    }
  }

  return (
    <section className="auth-layout">
      <div className="auth-panel">
        <div className="section-title">
          <p className="eyebrow">Sign in</p>
          <h2>Access your dashboard</h2>
        </div>
        <Message error={error} />
        <form className="form-grid" onSubmit={submit}>
          <label>
            Username
            <input name="username" value={form.username} onChange={updateField} required />
          </label>
          <label>
            Password
            <input name="password" type="password" value={form.password} onChange={updateField} required />
          </label>
          <button className="primary-action" type="submit">Login</button>
        </form>
        <p className="auth-link">
          New organizer? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </section>
  );
}
