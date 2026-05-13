import React from "react";
import { Navigate } from "react-router-dom";
import { getRole, getToken } from "../services/auth.js";

export default function ProtectedRoute({ role, children }) {
  const token = getToken();
  const currentRole = getRole();

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (role && currentRole !== role) {
    return <Navigate to={currentRole === "ADMIN" ? "/admin" : "/organizer"} replace />;
  }

  return children;
}
