/**
 * 预约状态
 */
export const RESERVATION_STATUS = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
}

export const RESERVATION_STATUS_TEXT = {
  [RESERVATION_STATUS.PENDING]: '待确认',
  [RESERVATION_STATUS.CONFIRMED]: '已确认',
  [RESERVATION_STATUS.COMPLETED]: '已完成',
  [RESERVATION_STATUS.CANCELLED]: '已取消'
}

/**
 * 订单状态
 */
export const ORDER_STATUS = {
  PENDING: 'pending',
  PAID: 'paid',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
}

export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING]: '待支付',
  [ORDER_STATUS.PAID]: '已支付',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消'
}

/**
 * 餐桌状态
 */
export const TABLE_STATUS = {
  AVAILABLE: 'available',
  UNAVAILABLE: 'unavailable',
  OCCUPIED: 'occupied'
}

export const TABLE_STATUS_TEXT = {
  [TABLE_STATUS.AVAILABLE]: '可用',
  [TABLE_STATUS.UNAVAILABLE]: '不可用',
  [TABLE_STATUS.OCCUPIED]: '已占用'
}

/**
 * 用户角色
 */
export const USER_ROLE = {
  ADMIN: 'ADMIN',           // 管理员
  RECEPTIONIST: 'RECEPTIONIST', // 前台
  USER: 'USER',              // 普通用户（数据库中的角色编码）
  CUSTOMER: 'USER'            // 普通用户（别名，与数据库保持一致）
}

export const USER_ROLE_TEXT = {
  [USER_ROLE.ADMIN]: '管理员',
  [USER_ROLE.RECEPTIONIST]: '前台',
  [USER_ROLE.USER]: '普通用户',
  [USER_ROLE.CUSTOMER]: '普通用户'
}

