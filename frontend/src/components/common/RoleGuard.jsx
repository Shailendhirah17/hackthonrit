import React from 'react';
import { useAuthStore } from '../../store/useAuthStore';

export const RoleGuard = ({ permission, roles, children, fallback = null }) => {
  const { hasPermission, hasRole } = useAuthStore();

  let hasAccess = true;

  if (permission && !hasPermission(permission)) {
    hasAccess = false;
  }

  if (roles && !hasRole(roles)) {
    hasAccess = false;
  }

  if (!hasAccess) {
    return fallback;
  }

  return <>{children}</>;
};

export default RoleGuard;
