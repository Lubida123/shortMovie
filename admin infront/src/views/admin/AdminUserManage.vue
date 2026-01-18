<template>
  <div class="admin-user-manage-container">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="请输入用户名/手机号搜索" 
        class="search-input"
        @keyup.enter="fetchUserList"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" :icon="CirclePlus" @click="handleAddUser">新增用户</el-button>
      <el-button type="warning" :icon="Refresh" @click="fetchUserList">刷新列表</el-button>
    </div>

    <!-- 用户列表 -->
    <div class="table-wrapper">
      <el-table 
        :data="filteredUserList" 
        border 
        stripe 
        class="user-table"
        v-loading="loading"
        height="calc(100vh - 280px)"
      >
        <el-table-column prop="id" label="用户ID" width="100" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="scope">
            <span class="phone-number">{{ formatPhoneNumber(scope.row.phone) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="用户角色" min-width="100">
          <template #default="scope">
            <el-tag class="role-tag" :type="scope.row.role === 'admin' ? 'danger' : 'success'">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" min-width="100">
          <template #default="scope">
            <el-tag class="status-tag" :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleEditUser(scope.row)"
                :icon="Edit"
                class="action-btn"
              >
                编辑
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="handleChangeStatus(scope.row)"
                :icon="scope.row.status === 'active' ? Lock : Unlock"
                class="action-btn"
              >
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDeleteUser(scope.row)"
                :icon="Delete"
                class="action-btn"
                v-if="scope.row.role !== 'admin'"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      />
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog 
      v-model="userDialogVisible" 
      :title="isEditMode ? '编辑用户' : '新增用户'" 
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
        class="user-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名" 
            class="form-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="userForm.phone" 
            placeholder="请输入手机号" 
            class="form-input"
            maxlength="11"
          >
            <template #prefix>
              <el-icon><Iphone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select 
            v-model="userForm.role" 
            placeholder="请选择角色" 
            class="form-select"
          >
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-select 
            v-model="userForm.status" 
            placeholder="请选择状态" 
            class="form-select"
          >
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item 
          label="密码" 
          prop="password"
          v-if="!isEditMode"
        >
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码" 
            class="form-input"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">密码长度6-20位，建议包含字母和数字</div>
        </el-form-item>
        <el-form-item v-else>
          <div class="password-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>编辑用户时不修改密码，如需修改请在密码重置功能中操作</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSaveUser" :loading="savingUser">
            {{ isEditMode ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  CirclePlus, 
  Refresh, 
  Delete, 
  Edit,
  Lock,
  Unlock,
  User,
  Iphone,
  InfoFilled
} from '@element-plus/icons-vue'
import { 
  getUserList as apiGetUserList,
  addUser as apiAddUser,
  editUser as apiEditUser,
  deleteUser as apiDeleteUser,
  changeUserStatus as apiChangeUserStatus
} from '@/api/admin/userApi'

// 加载状态
const loading = ref(false)
const savingUser = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 用户列表数据
const userList = ref([])

// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const userDialogVisible = ref(false)
const isEditMode = ref(false)
const userFormRef = ref(null)
const userForm = ref({
  id: '',
  username: '',
  phone: '',
  role: 'user',
  status: 'active',
  password: ''
})

// 角色映射
const roleMap = {
  'admin': '管理员',
  'user': '普通用户'
}

// 状态映射
const statusMap = {
  'active': '正常',
  'disabled': '禁用'
}

// 获取角色名称
const getRoleName = (role) => {
  return roleMap[role] || role
}

// 获取状态名称
const getStatusName = (status) => {
  return statusMap[status] || status
}

// 格式化手机号显示
const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1 **** $3')
}

// 过滤用户列表（搜索功能）
const filteredUserList = computed(() => {
  if (!searchKeyword.value) {
    return userList.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return userList.value.filter(user => 
    user.username.toLowerCase().includes(keyword) || 
    user.phone.includes(keyword)
  )
})

// 表单校验规则
const userFormRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true
    const res = await apiGetUserList({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value
    })
    userList.value = res.list || res // 适配两种格式的返回数据
    pagination.value.total = res.total || res.length || 0
  } catch (error) {
    // 如果API调用失败，使用本地数据
    console.log('使用本地数据...')
    userList.value = [
      {
        id: 1,
        username: '测试用户1',
        phone: '13800138000',
        role: 'user',
        status: 'active',
        createTime: '2026-01-01 10:00:00'
      },
      {
        id: 2,
        username: '管理员',
        phone: '13800138001',
        role: 'admin',
        status: 'active',
        createTime: '2026-01-02 10:00:00'
      },
      {
        id: 3,
        username: '张三',
        phone: '13900139000',
        role: 'user',
        status: 'disabled',
        createTime: '2026-01-03 14:30:00'
      },
      {
        id: 4,
        username: '李四',
        phone: '13600136000',
        role: 'user',
        status: 'active',
        createTime: '2026-01-04 09:15:00'
      },
      {
        id: 5,
        username: '王五',
        phone: '13500135000',
        role: 'user',
        status: 'active',
        createTime: '2026-01-05 16:45:00'
      }
    ]
    pagination.value.total = userList.value.length
  } finally {
    loading.value = false
  }
}

// 分页切换
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  fetchUserList()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  fetchUserList()
}

// 新增用户
const handleAddUser = () => {
  isEditMode.value = false
  // 重置表单
  userForm.value = {
    id: '',
    username: '',
    phone: '',
    role: 'user',
    status: 'active',
    password: ''
  }
  userDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (row) => {
  isEditMode.value = true
  // 赋值表单（不包含密码）
  userForm.value = {
    id: row.id,
    username: row.username,
    phone: row.phone,
    role: row.role,
    status: row.status,
    password: '' // 编辑时不显示密码
  }
  userDialogVisible.value = true
}

// 保存用户（新增/编辑） - 修复后的版本
const handleSaveUser = async () => {
  try {
    // 表单验证
    await userFormRef.value.validate()
    
    savingUser.value = true
    
    // 构建提交数据
    const submitData = { ...userForm.value }
    
    if (isEditMode.value) {
      // 编辑用户 - 移除密码字段（编辑时不修改密码）
      delete submitData.password
      
      // 调用编辑API
      await apiEditUser(submitData)
      ElMessage.success('用户信息更新成功')
      
      // 编辑成功后，直接刷新列表，从模拟API获取最新数据
      fetchUserList()
    } else {
      // 新增用户 - 必须包含密码
      if (!submitData.password) {
        ElMessage.error('请填写密码')
        savingUser.value = false
        return
      }
      
      // 调用新增API
      await apiAddUser(submitData)
      ElMessage.success('用户创建成功')
      
      // 新增成功后，重置分页到第一页并刷新列表
      // 这样可以看到最新添加的用户
      pagination.value.pageNum = 1
      fetchUserList()
    }
    
    userDialogVisible.value = false
    resetForm()
    
  } catch (error) {
    const errorMsg = isEditMode.value ? '更新用户失败' : '创建用户失败'
    ElMessage.error(error.message || errorMsg)
  } finally {
    savingUser.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  resetForm()
  userDialogVisible.value = false
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// 变更用户状态 - 修复后的版本
const handleChangeStatus = async (row) => {
  const newStatus = row.status === 'active' ? 'disabled' : 'active'
  const action = newStatus === 'active' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.username}" 吗？`,
      `${action}用户`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API
    await apiChangeUserStatus({
      id: row.id,
      status: newStatus
    })
    
    // 不再手动更新本地状态，直接刷新列表
    fetchUserList()
    
    ElMessage.success(`用户${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}用户失败`)
    }
  }
}

// 删除用户 - 修复后的版本
const handleDeleteUser = async (row) => {
  // 管理员用户不能删除
  if (row.role === 'admin') {
    ElMessage.warning('管理员用户不能删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可撤销！`,
      '删除用户',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    // 调用API
    await apiDeleteUser(row.id)
    
    // 不再手动移除本地数据，直接刷新列表
    fetchUserList()
    
    ElMessage.success('用户删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style lang="less" scoped>
@dy-bg-body: #121212;
@dy-bg-container: #161618;
@dy-bg-elevated: #252526;
@dy-bg-hover: #2D2D2D;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-text-tertiary: rgba(255, 255, 255, 0.55);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);
@dy-brand-red: #FE2C55;
@dy-brand-cyan: #25F4EE;

// 标签颜色变量
@tag-admin-bg: rgba(254, 44, 85, 0.3);
@tag-admin-text: #FF6B8B;
@tag-admin-border: rgba(254, 44, 85, 0.5);
@tag-user-bg: rgba(103, 194, 58, 0.3);
@tag-user-text: #85D475;
@tag-user-border: rgba(103, 194, 58, 0.5);
@tag-active-bg: rgba(103, 194, 58, 0.3);
@tag-active-text: #85D475;
@tag-disabled-bg: rgba(144, 147, 153, 0.3);
@tag-disabled-text: #A8ABB2;
@tag-disabled-border: rgba(144, 147, 153, 0.5);

.admin-user-manage-container {
  min-height: calc(100vh - 80px);
  background: @dy-bg-body;
  padding: 20px;
  color: @dy-text-primary;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
}

// 操作栏样式
.operation-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: @dy-bg-container;
  border-radius: 8px;
  border: @dy-border-default;
  flex-shrink: 0;

  .search-input {
    flex: 1;
    max-width: 300px;
    
    :deep(.el-input__wrapper) {
      background: @dy-bg-elevated;
      border: @dy-border-default;
      box-shadow: none;
      border-radius: 6px;
      
      &:hover {
        border-color: rgba(37, 244, 238, 0.3);
      }
      
      &:focus-within {
        border-color: @dy-brand-cyan;
        box-shadow: 0 0 0 1px rgba(37, 244, 238, 0.2);
      }
    }
    
    :deep(.el-input__inner) {
      color: @dy-text-primary;
      
      &::placeholder {
        color: @dy-text-tertiary;
      }
    }
  }
}

// 表格包装器
.table-wrapper {
  flex: 1;
  background: @dy-bg-container;
  border: @dy-border-default;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  overflow: hidden;
  min-height: 0;
}

// 表格样式
.user-table {
  width: 100%;
  --el-table-text-color: @dy-text-primary;
  --el-table-row-hover-bg-color: rgba(37, 244, 238, 0.08);
  --el-table-header-text-color: @dy-text-secondary;
  --el-table-border-color: rgba(255, 255, 255, 0.08);
  font-size: 13px;

  :deep(.el-table__header-wrapper) {
    th {
      background: @dy-bg-elevated !important;
      font-weight: 600;
      font-size: 14px;
      padding: 14px 8px !important;
      border-color: rgba(255, 255, 255, 0.08) !important;
      
      .cell {
        color: @dy-text-secondary !important;
        font-weight: 600;
      }
    }
  }

  :deep(.el-table__body-wrapper) {
    .el-table__row {
      transition: background-color 0.2s ease;
      
      &:hover {
        .el-table__cell {
          background: rgba(37, 244, 238, 0.08) !important;
        }
      }
    }
    
    .el-table__cell {
      padding: 12px 8px !important;
      border-color: rgba(255, 255, 255, 0.08) !important;
      
      &.is-right {
        .cell {
          display: flex;
          justify-content: flex-end;
          gap: 8px;
        }
      }
    }
  }
  
  // 手机号样式
  .phone-number {
    font-family: "DIN Condensed", monospace;
    color: @dy-text-secondary;
  }
  
  // 角色标签样式
  .role-tag {
    font-weight: 600 !important;
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    border: none !important;
    min-width: 60px;
    text-align: center;
    transition: all 0.2s ease;
    cursor: default;
    
    // 管理员标签
    &.el-tag--danger {
      background: @tag-admin-bg !important;
      color: @tag-admin-text !important;
      border: 1px solid @tag-admin-border !important;
      
      &:hover {
        background: rgba(254, 44, 85, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(254, 44, 85, 0.2);
      }
    }
    
    // 普通用户标签
    &.el-tag--success {
      background: @tag-user-bg !important;
      color: @tag-user-text !important;
      border: 1px solid @tag-user-border !important;
      
      &:hover {
        background: rgba(103, 194, 58, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
      }
    }
  }
  
  // 状态标签样式
  .status-tag {
    font-weight: 600 !important;
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    border: none !important;
    min-width: 60px;
    text-align: center;
    transition: all 0.2s ease;
    cursor: default;
    
    // 正常状态
    &.el-tag--success {
      background: @tag-active-bg !important;
      color: @tag-active-text !important;
      border: 1px solid @tag-user-border !important;
      
      &:hover {
        background: rgba(103, 194, 58, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
      }
    }
    
    // 禁用状态
    &.el-tag--info {
      background: @tag-disabled-bg !important;
      color: @tag-disabled-text !important;
      border: 1px solid @tag-disabled-border !important;
      
      &:hover {
        background: rgba(144, 147, 153, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(144, 147, 153, 0.2);
      }
    }
  }
  
  // 操作按钮组
  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
    
    .action-btn {
      min-width: 60px;
      height: 28px;
      padding: 0 10px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      transition: all 0.2s ease;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 分页容器
.pagination-container {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  padding: 16px;
  background: @dy-bg-container;
  border-radius: 8px;
  border: @dy-border-default;
  
  :deep(.el-pagination) {
    --el-pagination-text-color: @dy-text-secondary;
    --el-pagination-button-disabled-bg-color: @dy-bg-elevated;
    --el-pagination-bg-color: @dy-bg-container;
    --el-pagination-button-bg-color: @dy-bg-elevated;
    --el-pagination-hover-color: @dy-brand-cyan;
    
    .btn-prev, .btn-next {
      cursor: pointer;
      
      &:hover:not(:disabled) {
        background-color: rgba(37, 244, 238, 0.1) !important;
      }
    }
    
    .el-pager li {
      background: @dy-bg-elevated;
      color: @dy-text-secondary;
      cursor: pointer;
      
      &:hover {
        color: @dy-brand-cyan !important;
        background-color: rgba(37, 244, 238, 0.1) !important;
      }
      
      &.active {
        background: @dy-brand-red;
        color: white;
        cursor: default;
      }
    }
    
    .el-pagination__jump {
      .el-input__wrapper {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        
        .el-input__inner {
          color: @dy-text-secondary;
        }
      }
    }
  }
}

// 弹窗样式
:deep(.el-dialog) {
  background: @dy-bg-container;
  border: @dy-border-default;
  border-radius: 12px;
  --el-dialog-text-color: @dy-text-primary;
  --el-dialog-title-font-size: 16px;
  
  .el-dialog__header {
    border-bottom: @dy-border-default;
    padding-bottom: 16px;
    margin-bottom: 20px;
    
    .el-dialog__title {
      color: @dy-text-primary;
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: @dy-text-tertiary;
        
        &:hover {
          color: @dy-brand-red;
        }
      }
    }
  }
  
  .user-form {
    .el-form-item {
      margin-bottom: 20px;
      
      .el-form-item__label {
        color: @dy-text-secondary;
        font-weight: 500;
        padding-right: 16px;
      }
      
      .form-input, .form-select {
        width: 100%;
        
        :deep(.el-input__wrapper) {
          background: @dy-bg-elevated;
          border: @dy-border-default;
          border-radius: 6px;
          box-shadow: none;
          
          &:hover {
            border-color: rgba(37, 244, 238, 0.3);
          }
          
          &:focus-within {
            border-color: @dy-brand-cyan;
            box-shadow: 0 0 0 1px rgba(37, 244, 238, 0.2);
          }
          
          .el-input__inner {
            color: @dy-text-primary;
            
            &::placeholder {
              color: @dy-text-tertiary;
            }
          }
        }
      }
      
      .form-tip {
        font-size: 12px;
        color: @dy-text-tertiary;
        margin-top: 4px;
        line-height: 1.4;
      }
      
      .password-tip {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        padding: 8px 12px;
        background: rgba(230, 162, 60, 0.1);
        border-radius: 6px;
        border: 1px solid rgba(230, 162, 60, 0.2);
        
        .el-icon {
          color: #E6A23C;
          font-size: 16px;
          margin-top: 2px;
        }
        
        span {
          font-size: 13px;
          color: #E6A23C;
          line-height: 1.5;
        }
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    border-top: @dy-border-default;
    padding-top: 16px;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .admin-user-manage-container {
    padding: 12px;
  }
  
  .operation-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 12px;
    
    .search-input {
      max-width: 100%;
    }
  }
  
  .table-wrapper {
    padding: 12px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
    justify-content: flex-start !important;
    
    .action-btn {
      flex: 1;
      min-width: 0;
    }
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 400px;
    
    .user-form {
      .el-form-item__label {
        padding-right: 12px;
        font-size: 14px;
      }
    }
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .action-buttons {
    flex-direction: column;
    gap: 6px;
    
    .action-btn {
      width: 100%;
    }
  }
}
</style>