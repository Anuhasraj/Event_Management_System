import React from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { clearSession, getUsername } from "./services/auth.js";

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const username = getUsername();
  const isAuthPage = location.pathname === "/login" || location.pathname === "/register";
  const userInitial = username ? username.charAt(0).toUpperCase() : "U";

  function logout() {
    clearSession();
    navigate("/login");
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        {!isAuthPage && (
          <div>
            <h1>UEMS</h1>
            <p className="topbar-subtitle">Uni Event Management System</p>
          </div>
        )}
        {username && (
          <div className="user-pill">
            <span className="user-avatar">{userInitial}</span>
            <span>{username}</span>
            <button type="button" onClick={logout}>Logout</button>
          </div>
        )}
      </header>
      <Outlet />
    </main>
  );
}
