export function saveSession(data) {
  localStorage.setItem("uems_token", data.token);
  localStorage.setItem("uems_username", data.username);
  localStorage.setItem("uems_role", data.role);
}

export function clearSession() {
  localStorage.removeItem("uems_token");
  localStorage.removeItem("uems_username");
  localStorage.removeItem("uems_role");
}

export function getToken() {
  return localStorage.getItem("uems_token");
}

export function getRole() {
  return localStorage.getItem("uems_role");
}

export function getUsername() {
  return localStorage.getItem("uems_username");
}
