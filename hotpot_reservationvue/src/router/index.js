import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  // 管理端认证
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/auth/Login.vue'),
    meta: { title: '管理员登录' }
  },
  // 注册路由已移至 /customer/register
  // {
  //   path: '/register',
  //   name: 'Register',
  //   component: () => import('@/views/customer/auth/Register.vue'),
  //   meta: { title: '注册' }
  // },
  {
    path: '/customer/login',
    name: 'CustomerLogin',
    component: () => import('@/views/customer/auth/Login.vue'),
    meta: { title: '用户登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/dashboard/Index.vue'),
        meta: { title: '首页' }
      },
      // 业务管理
      // 系统管理
      {
        path: 'system/user/user-role',
        name: 'UserRoleList',
        component: () => import('@/views/admin/system/user/UserRole.vue'),
        meta: { title: '用户管理', parentMenu: '系统设置' }
      },
      {
        path: 'system/user',
        name: 'UserList',
        component: () => import('@/views/admin/system/user/List.vue'),
        meta: { title: '角色管理', parentMenu: '系统设置' }
      },
      {
        path: 'system/user/role',
        name: 'RoleList',
        component: () => import('@/views/admin/system/user/Role.vue'),
        meta: { title: '权限设置', parentMenu: '系统设置' }
      },
      {
        path: 'system/log/operation',
        name: 'OperationLog',
        component: () => import('@/views/admin/system/log/Operation.vue'),
        meta: { title: '操作日志', parentMenu: '系统设置', requiresAuth: true }
      },
      // 业务管理 - 预约
      {
        path: 'business/reservation',
        name: 'ReservationList',
        component: () => import('@/views/admin/business/reservation/List.vue'),
        meta: { title: '预约列表' }
      },
      {
        path: 'business/reservation/calendar',
        name: 'ReservationCalendar',
        component: () => import('@/views/admin/business/reservation/Calendar.vue'),
        meta: { title: '预约日历' }
      },
      {
        path: 'business/reservation/statistics',
        name: 'ReservationStatistics',
        component: () => import('@/views/admin/business/reservation/Statistics.vue'),
        meta: { title: '预约统计' }
      },
      // 业务管理 - 餐桌
      {
        path: 'business/table',
        name: 'TableList',
        component: () => import('@/views/admin/business/table/List.vue'),
        meta: { title: '餐桌管理' }
      },
      {
        path: 'business/table/layout',
        name: 'TableLayout',
        component: () => import('@/views/admin/business/table/Layout.vue'),
        meta: { title: '餐桌布局图' }
      },
      // 业务管理 - 菜单
      {
        path: 'business/menu/category',
        name: 'MenuCategoryList',
        component: () => import('@/views/admin/business/menu/category/List.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'business/menu/item',
        name: 'MenuItemList',
        component: () => import('@/views/admin/business/menu/item/List.vue'),
        meta: { title: '菜品管理' }
      },
      // 业务管理 - 订单
      {
        path: 'business/order',
        name: 'OrderList',
        component: () => import('@/views/admin/business/order/List.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'business/order/statistics',
        name: 'OrderStatistics',
        component: () => import('@/views/admin/business/order/Statistics.vue'),
        meta: { title: '订单统计' }
      },
      {
        path: 'business/order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/admin/business/order/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      // 业务管理 - 支付
      {
        path: 'business/payment',
        name: 'PaymentList',
        component: () => import('@/views/admin/business/payment/List.vue'),
        meta: { title: '支付管理' }
      },
      // 业务管理 - 评论
      {
        path: 'business/review',
        name: 'ReviewList',
        component: () => import('@/views/admin/business/review/List.vue'),
        meta: { title: '评论管理' }
      },
      // 业务管理 - 客户
      {
        path: 'business/customer',
        name: 'CustomerList',
        component: () => import('@/views/admin/business/customer/List.vue'),
        meta: { title: '会员管理' }
      },
      // 业务管理 - 公告
      {
        path: 'business/announcement',
        name: 'AdminAnnouncementList',
        component: () => import('@/views/admin/business/announcement/List.vue'),
        meta: { title: '公告管理' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/profile/Index.vue'),
        meta: { title: '个人中心' }
      },
      // 报表统计
      {
        path: 'report/sales',
        name: 'SalesReport',
        component: () => import('@/views/admin/report/sales/Index.vue'),
        meta: { title: '销售报表', parentMenu: '报表统计' }
      },
      {
        path: 'report/customer',
        name: 'CustomerReport',
        component: () => import('@/views/admin/report/customer/Index.vue'),
        meta: { title: '客户分析', parentMenu: '报表统计' }
      },
      {
        path: 'report/inventory',
        name: 'InventoryReport',
        component: () => import('@/views/admin/report/inventory/Index.vue'),
        meta: { title: '库存报表', parentMenu: '报表统计' }
      },
    ]
  },
  {
    path: '/customer/register',
    name: 'CustomerRegister',
    component: () => import('@/views/customer/auth/Register.vue'),
    meta: { title: '用户注册' }
  },
  {
    path: '/customer',
    component: () => import('@/layouts/CustomerLayout.vue'),
    redirect: '/customer/home',
    children: [
      {
        path: 'home',
        name: 'CustomerHome',
        component: () => import('@/views/customer/home/Index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'menu',
        name: 'CustomerMenu',
        component: () => import('@/views/customer/menu/Index.vue'),
        meta: { title: '菜单' }
      },
      {
        path: 'menu/category',
        name: 'MenuCategory',
        component: () => import('@/views/customer/menu/Category.vue'),
        meta: { title: '分类浏览' }
      },
      // 菜品搜索功能可能在Index.vue中实现
      // {
      //   path: 'menu/search',
      //   name: 'MenuSearch',
      //   component: () => import('@/views/customer/menu/Search.vue'),
      //   meta: { title: '菜品搜索' }
      // },
      {
        path: 'reservation',
        name: 'CustomerReservation',
        component: () => import('@/views/customer/reservation/Index.vue'),
        meta: { title: '预约', requiresAuth: true }
      },
      {
        path: 'reservation/list',
        name: 'MyReservations',
        component: () => import('@/views/customer/reservation/MyReservation.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      {
        path: 'reservation/success',
        name: 'ReservationSuccess',
        component: () => import('@/views/customer/reservation/Success.vue'),
        meta: { title: '预约成功' }
      },
      {
        path: 'reservation/fail',
        name: 'ReservationFail',
        component: () => import('@/views/customer/reservation/Fail.vue'),
        meta: { title: '预约失败' }
      },
      // 预约成功页面可能尚未创建
      // {
      //   path: 'reservation/success',
      //   name: 'ReservationSuccess',
      //   component: () => import('@/views/customer/reservation/Success.vue'),
      //   meta: { title: '预约成功' }
      // },
      {
        path: 'order',
        name: 'CustomerOrder',
        component: () => import('@/views/customer/order/Index.vue'),
        meta: { title: '点餐', requiresAuth: true }
      },
      {
        path: 'order/cart',
        name: 'OrderCart',
        component: () => import('@/views/customer/order/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'order/list',
        name: 'MyOrders',
        component: () => import('@/views/customer/order/MyOrder.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'order/detail/:id',
        name: 'CustomerOrderDetail',
        component: () => import('@/views/customer/order/detail/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'order/checkout',
        name: 'OrderCheckout',
        component: () => import('@/views/customer/order/Checkout.vue'),
        meta: { title: '结算', requiresAuth: true }
      },
      {
        path: 'payment',
        name: 'PaymentIndex',
        component: () => import('@/views/customer/payment/Index.vue'),
        meta: { title: '支付', requiresAuth: true }
      },
      {
        path: 'payment/success',
        name: 'PaymentSuccess',
        component: () => import('@/views/customer/payment/Success.vue'),
        meta: { title: '支付成功' }
      },
      {
        path: 'payment/fail',
        name: 'PaymentFail',
        component: () => import('@/views/customer/payment/Fail.vue'),
        meta: { title: '支付失败' }
      },
      {
        path: 'review',
        name: 'ReviewIndex',
        component: () => import('@/views/customer/review/Index.vue'),
        meta: { title: '评价' }
      },
      {
        path: 'review/my',
        name: 'MyReviews',
        component: () => import('@/views/customer/review/MyReview.vue'),
        meta: { title: '我的评价', requiresAuth: true }
      },
      {
        path: 'announcement',
        name: 'AnnouncementList',
        component: () => import('@/views/customer/announcement/AnnouncementList.vue'),
        meta: { title: '公告列表' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/customer/profile/Index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      // 帮助中心相关页面
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/customer/about/About.vue'),
        meta: { title: '关于我们' }
      }
      // 以下页面可能尚未创建
      // {
      //   path: 'help/faq',
      //   name: 'FAQ',
      //   component: () => import('@/views/customer/help/FAQ.vue'),
      //   meta: { title: '常见问题' }
      // },
      // {
      //   path: 'help/contact',
      //   name: 'Contact',
      //   component: () => import('@/views/customer/help/Contact.vue'),
      //   meta: { title: '联系我们' }
      // },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 火锅店预约系统`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 如果是用户端路由，跳转到用户端登录页
    if (to.path.startsWith('/customer')) {
      next({ name: 'CustomerLogin', query: { redirect: to.fullPath } })
    } else {
      // 管理端路由跳转到管理端登录页
      next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    }
    return
  }
  
  // 已登录用户，检查角色权限
  if (userStore.isLoggedIn) {
    // 判断是否为管理端路由
    const adminRoutes = ['/dashboard', '/business', '/system', '/profile', '/report']
    const isAdminRoute = to.path === '/' || adminRoutes.some(route => to.path.startsWith(route))
    
    if (isAdminRoute) {
      if (!userStore.isAdmin) {
        ElMessage.warning('您没有权限访问管理端，请使用管理员账号登录')
        // 跳转到管理端登录页，而不是用户端首页
        next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
        return
      }
    }
    
    // 访问用户端需要登录的页面
    if (to.path.startsWith('/customer') && to.meta.requiresAuth) {
      // 如果是纯管理员（没有 CUSTOMER 角色），提示应该使用管理端
      if (userStore.isAdmin && !userStore.isCustomer) {
        ElMessage.warning('管理员账号请使用管理端登录')
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router

