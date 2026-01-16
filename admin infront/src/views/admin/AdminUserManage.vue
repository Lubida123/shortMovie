<template>
  <div class="admin-user-manage-container">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="请输入用户名/手机号搜索" 
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" :icon="CirclePlus" @click="handleAddUser">新增用户</el-button>
      <el-button type="warning" :icon="Refresh" @click="fetchUserList">刷新列表</el-button>
    </div>

    <!-- 用户列表 -->
    <el-table 
      :data="userList" 
      border 
      stripe 
      class="user-table"
      v-loading="loading"
    >
      <el-table-column prop="id" label="用户ID" width="100" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="role" label="用户角色" min-width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'success'">
            {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="账号状态" min-width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
            {{ scope.row.status === 'active' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" min-width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEditUser(scope.row)">编辑</el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click="handleChangeStatus(scope.row)"
          >
            {{ scope.row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDeleteUser(scope.row.id)"
            :icon="Delete"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.pageNum"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      class="pagination"
    >
    </el-pagination>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog 
      v-model="userDialogVisible" 
      title="新增用户" 
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item 
          label="密码" 
          prop="password"
          v-if="!isEdit"
        >
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, CirclePlus, Refresh, Delete } from '@element-plus/icons-vue'
import { 
  getUserList as apiGetUserList,  // 重命名导入的函数
  addUser, 
  editUser, 
  deleteUser, 
  changeUserStatus 
} from '@/api/admin/userApi'

// 加载状态
const loading = ref(false)
// 搜索关键词
const searchKeyword = ref('')
// 用户列表
const userList = ref([])
// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const userDialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)
const userForm = ref({
  id: '',
  username: '',
  phone: '',
  role: 'user',
  status: 'active',
  password: ''
})

// 表单校验规则
const userFormRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

// 获取用户列表
const fetchUserList = async () => {  // 改名：getUserList -> fetchUserList
  try {
    loading.value = true
    const res = await apiGetUserList({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value
    })
    userList.value = res.list
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 分页切换
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  fetchUserList()  // 修改为 fetchUserList
}
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  fetchUserList()  // 修改为 fetchUserList
}

// 新增用户
const handleAddUser = () => {
  isEdit.value = false
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
  isEdit.value = true
  // 赋值表单
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

// 保存用户（新增/编辑）
const handleSaveUser = async () => {
  try {
    await userFormRef.value.validate()
    if (isEdit.value) {
      // 编辑用户
      await editUser(userForm.value)
      ElMessage.success('编辑用户成功')
    } else {
      // 新增用户
      await addUser(userForm.value)
      ElMessage.success('新增用户成功')
    }
    userDialogVisible.value = false
    fetchUserList() // 刷新列表
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑用户失败' : '新增用户失败')
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  userFormRef.value.resetFields()
  userDialogVisible.value = false
}

// 变更用户状态
const handleChangeStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 'active' ? '禁用' : '启用'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await changeUserStatus({
      id: row.id,
      status: row.status === 'active' ? 'disabled' : 'active'
    })
    ElMessage.success(`用户${row.status === 'active' ? '禁用' : '启用'}成功`)
    fetchUserList()  // 修改为 fetchUserList
  } catch (error) {
    ElMessage.info('已取消操作')
  }
}

// 删除用户
const handleDeleteUser = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该用户吗？此操作不可撤销！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    await deleteUser(id)
    ElMessage.success('删除用户成功')
    fetchUserList()  // 修改为 fetchUserList
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

// 初始化加载列表
onMounted(() => {
  fetchUserList()  // 修改为 fetchUserList
})
</script>

<style lang="less" scoped>
@dy-bg-body: #121212;
@dy-bg-container: #161618;
@dy-bg-elevated: #252526;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);

.admin-user-manage-container {
  min-height: 100vh;
  background: @dy-bg-body;
  padding: 20px;
  color: @dy-text-primary;

  // 操作栏样式
  .operation-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px;
    background: @dy-bg-container;
    border-radius: 8px;
    border: @dy-border-default;

    .search-input {
      width: 300px;
      background: @dy-bg-elevated;
      border: @dy-border-default;
      ::v-deep(.el-input__wrapper) {
        background: transparent;
        box-shadow: none;
      }
    }
  }

  // 表格样式
  .user-table {
    background: @dy-bg-container;
    border: @dy-border-default;
    --el-table-text-color: @dy-text-primary;
    --el-table-row-hover-bg-color: @dy-bg-elevated;
    --el-table-header-text-color: @dy-text-secondary;
    --el-table-border-color: rgba(255, 255, 255, 0.08);

    ::v-deep(.el-table__header) {
      th {
        background: @dy-bg-elevated !important;
      }
    }
  }

  // 分页样式
  .pagination {
    margin-top: 20px;
    text-align: right;
    ::v-deep(.el-pagination) {
      --el-pagination-text-color: @dy-text-secondary;
    }
  }

  // 弹窗样式
  ::v-deep(.el-dialog) {
    background: @dy-bg-container;
    border: @dy-border-default;
    --el-dialog-text-color: @dy-text-primary;

    .el-dialog__header {
      border-bottom: @dy-border-default;
      padding-bottom: 12px;
    }

    .el-form {
      .el-form-item__label {
        color: @dy-text-secondary;
      }

      .el-input, .el-select {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        ::v-deep(.el-input__wrapper) {
          background: transparent;
          box-shadow: none;
        }
      }
    }
  }
}
</style>