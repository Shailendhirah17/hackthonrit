import { create } from 'zustand';
import api from '../api/client';

const DEMO_USERS = {
  SUPER_ADMIN: {
    userId: 1,
    name: "Dr. Rajesh Verma",
    email: "superadmin@gramdrishti.gov.in",
    role: "SUPER_ADMIN",
    department: "Ministry of Rural Development (MoRD)",
    permissions: [
      "USER_MANAGEMENT", "ROLE_MANAGEMENT", "PROJECT_CREATE_EDIT", "PROJECT_VIEW",
      "REQUIREMENT_CREATE", "FIELD_DATA_ENTRY", "AI_ANALYSIS", "GIS_ANALYSIS",
      "REPORTS", "FILE_UPLOAD", "FILE_DELETE", "AUDIT_LOGS", "SYSTEM_SETTINGS"
    ]
  },
  ADMIN: {
    userId: 2,
    name: "Pooja Sharma (IAS)",
    email: "admin@gramdrishti.gov.in",
    role: "ADMIN",
    department: "State Planning Board, Maharashtra",
    permissions: [
      "USER_MANAGEMENT", "PROJECT_CREATE_EDIT", "PROJECT_VIEW", "REQUIREMENT_CREATE",
      "FIELD_DATA_ENTRY", "AI_ANALYSIS", "GIS_ANALYSIS", "REPORTS", "FILE_UPLOAD",
      "FILE_DELETE", "AUDIT_LOGS", "SYSTEM_SETTINGS"
    ]
  },
  PROJECT_MANAGER: {
    userId: 3,
    name: "Vikramaditya Rao",
    email: "pm@gramdrishti.gov.in",
    role: "PROJECT_MANAGER",
    department: "PMGSY Infrastructure Directorate",
    permissions: [
      "PROJECT_CREATE_EDIT", "PROJECT_VIEW", "REQUIREMENT_CREATE", "FIELD_DATA_ENTRY",
      "AI_ANALYSIS", "GIS_ANALYSIS", "REPORTS", "FILE_UPLOAD", "FILE_DELETE", "AUDIT_LOGS"
    ]
  },
  ANALYST: {
    userId: 4,
    name: "Ananya Sengupta",
    email: "analyst@gramdrishti.gov.in",
    role: "ANALYST",
    department: "GIS & Remote Sensing Cell",
    permissions: [
      "PROJECT_VIEW", "REQUIREMENT_CREATE", "AI_ANALYSIS", "GIS_ANALYSIS",
      "REPORTS", "FILE_UPLOAD"
    ]
  },
  FIELD_OFFICER: {
    userId: 5,
    name: "Suresh Naik",
    email: "field@gramdrishti.gov.in",
    role: "FIELD_OFFICER",
    department: "Panchayat Development Office, Gadchiroli",
    permissions: [
      "PROJECT_VIEW", "FIELD_DATA_ENTRY", "REPORTS", "FILE_UPLOAD"
    ]
  },
  VIEWER: {
    userId: 6,
    name: "Kavita Nair",
    email: "viewer@gramdrishti.gov.in",
    role: "VIEWER",
    department: "Citizen Oversight & Public Media Portal",
    permissions: [
      "PROJECT_VIEW", "GIS_ANALYSIS", "REPORTS"
    ]
  }
};

export const useAuthStore = create((set, get) => {
  // Load initial user from localStorage if present
  let initialUser = DEMO_USERS.SUPER_ADMIN;
  try {
    const savedUser = localStorage.getItem('gd_user');
    if (savedUser) initialUser = JSON.parse(savedUser);
  } catch (e) {}

  return {
    user: initialUser,
    token: localStorage.getItem('gd_access_token') || 'demo_jwt_token',
    isAuthenticated: true,
    isLoading: false,

    login: async (email, password) => {
      set({ isLoading: true });
      try {
        const response = await api.post('/auth/login', { email, password });
        if (response.success && response.data) {
          const authData = response.data;
          const userObj = {
            userId: authData.userId,
            name: authData.name,
            email: authData.email,
            role: authData.role,
            department: authData.department,
            permissions: authData.permissions || []
          };
          localStorage.setItem('gd_access_token', authData.accessToken);
          localStorage.setItem('gd_user', JSON.stringify(userObj));
          set({ user: userObj, token: authData.accessToken, isAuthenticated: true, isLoading: false });
          return { success: true };
        }
      } catch (err) {
        // If backend is offline or credentials error, fallback to demo user matching email
        for (const key of Object.keys(DEMO_USERS)) {
          if (DEMO_USERS[key].email === email) {
            const demoUser = DEMO_USERS[key];
            localStorage.setItem('gd_user', JSON.stringify(demoUser));
            set({ user: demoUser, token: 'demo_token', isAuthenticated: true, isLoading: false });
            return { success: true };
          }
        }
        set({ isLoading: false });
        return { success: false, message: err.message || 'Login failed' };
      }
      set({ isLoading: false });
      return { success: false, message: 'Invalid credentials' };
    },

    logout: async () => {
      try {
        await api.post('/auth/logout');
      } catch (e) {}
      localStorage.removeItem('gd_access_token');
      localStorage.removeItem('gd_user');
      set({ user: null, token: null, isAuthenticated: false });
    },

    // Quick demo switcher to test the UI for any role
    switchRole: async (roleKey) => {
      const targetUser = DEMO_USERS[roleKey] || DEMO_USERS.SUPER_ADMIN;
      localStorage.setItem('gd_user', JSON.stringify(targetUser));
      set({ user: targetUser, isAuthenticated: true });
      try {
        const response = await api.post('/auth/login', {
          email: targetUser.email,
          password: 'Password@123'
        });
        if (response.success && response.data?.accessToken) {
          localStorage.setItem('gd_access_token', response.data.accessToken);
          set({ token: response.data.accessToken });
        }
      } catch (e) {
        // Keep demo offline fallback
      }
    },

    hasPermission: (permissionName) => {
      const currentUser = get().user;
      if (!currentUser) return false;
      if (currentUser.role === 'SUPER_ADMIN') return true;
      return currentUser.permissions?.includes(permissionName) || false;
    },

    hasRole: (allowedRoles) => {
      const currentUser = get().user;
      if (!currentUser) return false;
      if (currentUser.role === 'SUPER_ADMIN') return true;
      if (Array.isArray(allowedRoles)) {
        return allowedRoles.includes(currentUser.role);
      }
      return currentUser.role === allowedRoles;
    }
  };
});
