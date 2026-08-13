/**
 * Permission management utility.
 */

export type Permission = string
export type Role = string

export interface PermissionConfig {
  roles: Record<Role, Permission[]>
  defaultRole: Role
}

const defaultConfig: PermissionConfig = {
  roles: {
    admin: ['*'], // Admin has all permissions
    user: [
      'study:read',
      'study:write',
      'journal:read',
      'journal:write',
      'room:read',
      'room:write',
      'gamification:read',
      'profile:read',
      'profile:write',
    ],
    guest: [
      'study:read',
      'journal:read',
      'room:read',
    ],
  },
  defaultRole: 'guest',
}

let config: PermissionConfig = defaultConfig
let currentRole: Role = defaultConfig.defaultRole

/**
 * Set permission configuration.
 *
 * @param newConfig Permission configuration
 */
export function setPermissionConfig(newConfig: PermissionConfig): void {
  config = newConfig
}

/**
 * Set current user role.
 *
 * @param role User role
 */
export function setCurrentRole(role: Role): void {
  currentRole = role
}

/**
 * Get current user role.
 *
 * @returns Current role
 */
export function getCurrentRole(): Role {
  return currentRole
}

/**
 * Check if current user has permission.
 *
 * @param permission Permission to check
 * @returns true if has permission
 */
export function hasPermission(permission: Permission): boolean {
  const rolePermissions = config.roles[currentRole] || []
  
  // Check for wildcard permission
  if (rolePermissions.includes('*')) {
    return true
  }
  
  // Check for exact permission
  if (rolePermissions.includes(permission)) {
    return true
  }
  
  // Check for wildcard pattern (e.g., 'study:*' matches 'study:read')
  const [resource] = permission.split(':')
  if (rolePermissions.includes(`${resource}:*`)) {
    return true
  }
  
  return false
}

/**
 * Check if current user has all permissions.
 *
 * @param permissions Permissions to check
 * @returns true if has all permissions
 */
export function hasAllPermissions(permissions: Permission[]): boolean {
  return permissions.every(permission => hasPermission(permission))
}

/**
 * Check if current user has any permission.
 *
 * @param permissions Permissions to check
 * @returns true if has any permission
 */
export function hasAnyPermission(permissions: Permission[]): boolean {
  return permissions.some(permission => hasPermission(permission))
}

/**
 * Get all permissions for current role.
 *
 * @returns Array of permissions
 */
export function getPermissions(): Permission[] {
  return config.roles[currentRole] || []
}

/**
 * Composable for permission management.
 */
export function usePermissions() {
  return {
    hasPermission,
    hasAllPermissions,
    hasAnyPermission,
    getPermissions,
    getCurrentRole,
    setCurrentRole,
  }
}