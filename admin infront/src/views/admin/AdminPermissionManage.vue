<template>
  <div class="admin-permission-manage-container">
    <!-- 角色列表 -->
    <div class="role-list-container">
      <div class="role-header">
        <h3>角色管理</h3>
        <el-button type="primary" :icon="CirclePlus" @click="handleAddRole">新增角色</el-button>
      </div>

      <el-table 
        :data="roleList" 
        border 
        stripe 
        class="role-table"
        v-loading="loading"
        @row-click="handleRoleClick"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="description" label="角色描述" min-width="200" />
        <el-table-column label="操作" min-width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditRole(scope.row)">编辑</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeleteRole(scope.row.roleCode)"
              :icon="Delete"
              :disabled="scope.row.roleCode === 'super_admin'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 权限配置区域 -->
    <div class="permission-config-container" v-if="selectedRole">
      <div class="permission-header">
        <h3>权限配置 - {{ selectedRole.roleName }}</h3>
        <el-button type="primary" @click="handleSavePermission">保存权限</el-button>
      </div>

      <!-- 权限树形结构 -->
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="permissionCode"
        :props="treeProps"
        :default-checked-keys="selectedPermissions"
        class="permission-tree"
      ></el-tree>
    </div>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog 
      v-model="roleDialogVisible" 
      title="新增角色" 
      width="400px"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        label-width="100px"
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input 
            v-model="roleForm.roleCode" 
            placeholder="请输入角色编码（如：admin）"
            :disabled="isEditRole"
          ></el-input>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            placeholder="请输入角色描述"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CirclePlus, Delete } from '@element-plus/icons-vue'
import { 
  getRoleList, 
  addRole, 
  editRole, 
  deleteRole,
  getPermissionTree,
  getRolePermission,
  saveRolePermission
} from '@/api/admin/userApi'

// 加载状态
const loading = ref(false)
// 角色列表
const roleList = ref([])
// 选中的角色
const selectedRole = ref(null)
// 权限树形数据
const permissionTree = ref([])
// 选中的权限
const selectedPermissions = ref([])
// 树配置
const treeProps = ref({
  label: 'permissionName',
  children: 'children'
})
const permissionTreeRef = ref(null)

// 角色弹窗相关
const roleDialogVisible = ref(false)
const isEditRole = ref(false)
const roleFormRef = ref(null)
const roleForm = ref({
  roleCode: '',
  roleName: '',
  description: ''
})

// 角色表单校验规则
const roleFormRules = ref({
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '角色编码仅支持字母、数字、下划线', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 100, message: '角色描述长度不超过 100 个字符', trigger: 'blur' }
  ]
})

// 获取角色列表
const getRoleListFn = async () => {
  try {
    loading.value = true
    const res = await getRoleList()
    roleList.value = res
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限树形结构
const getPermissionTreeFn = async () => {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res
  } catch (error) {
    ElMessage.error('获取权限列表失败')
  }
}

// 获取角色权限
const getRolePermissionFn = async (roleCode) => {
  try {
    const res = await getRolePermission(roleCode)
    selectedPermissions.value = res
    // 刷新树的选中状态
    nextTick(() => {
      permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
    })
  } catch (error) {
    ElMessage.error('获取角色权限失败')
  }
}

// 点击角色行
const handleRoleClick = (row) => {
  selectedRole.value = row
  getRolePermissionFn(row.roleCode)
}

// 表格行样式（标记选中行）
const tableRowClassName = ({ row }) => {
  return row.roleCode === selectedRole.value?.roleCode ? 'selected-row' : ''
}

// 新增角色
const handleAddRole = () => {
  isEditRole.value = false
  roleForm.value = {
    roleCode: '',
    roleName: '',
    description: ''
  }
  roleDialogVisible.value = true
}

// 编辑角色
const handleEditRole = (row) => {
  isEditRole.value = true
  roleForm.value = {
    roleCode: row.roleCode,
    roleName: row.roleName,
    description: row.description
  }
  roleDialogVisible.value = true
}

// 保存角色（新增/编辑）
const handleSaveRole = async () => {
  try {
    await roleFormRef.value.validate()
    if (isEditRole.value) {
      // 编辑角色
      await editRole(roleForm.value)
      ElMessage.success('编辑角色成功')
    } else {
      // 新增角色
      await addRole(roleForm.value)
      ElMessage.success('新增角色成功')
    }
    roleDialogVisible.value = false
    getRoleListFn() // 刷新角色列表
  } catch (error) {
    ElMessage.error(isEditRole.value ? '编辑角色失败' : '新增角色失败')
  }
}

// 删除角色
const handleDeleteRole = async (roleCode) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该角色吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    await deleteRole(roleCode)
    ElMessage.success('删除角色成功')
    getRoleListFn()
    // 如果删除的是当前选中角色，清空选中状态
    if (selectedRole.value?.roleCode === roleCode) {
      selectedRole.value = null
      selectedPermissions.value = []
    }
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

// 保存角色权限
const handleSavePermission = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请先选择角色')
    return
  }
  try {
    // 获取选中的权限节点
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    await saveRolePermission({
      roleCode: selectedRole.value.roleCode,
      permissionCodes: checkedKeys
    })
    ElMessage.success('保存权限成功')
    selectedPermissions.value = checkedKeys
  } catch (error) {
    ElMessage.error('保存权限失败')
  }
}

// 初始化
onMounted(() => {
  getRoleListFn()
  getPermissionTreeFn()
})
</script>

<style lang="less" scoped>
@dy-bg-body: #121212;
@dy-bg-container: #161618;
@dy-bg-elevated: #252526;
@dy-bg-hover: #2D2D2D;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);
@dy-brand-red: #FE2C55;

.admin-permission-manage-container {
  min-height: 100vh;
  background: @dy-bg-body;
  padding: 20px;
  color: @dy-text-primary;
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;

  // 角色列表区域
  .role-list-container {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 8px;
    padding: 20px;

    .role-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: @dy-border-default;
    }

    .role-table {
      --el-table-text-color: @dy-text-primary;
      --el-table-row-hover-bg-color: @dy-bg-elevated;
      --el-table-header-text-color: @dy-text-secondary;
      --el-table-border-color: rgba(255, 255, 255, 0.08);

      ::v-deep(.el-table__header) {
        th {
          background: @dy-bg-elevated !important;
        }
      }

      ::v-deep(.selected-row) {
        td {
          background: rgba(254, 44, 85, 0.1) !important;
          color: @dy-brand-red;
        }
      }
    }
  }

  // 权限配置区域
  .permission-config-container {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 8px;
    padding: 20px;

    .permission-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: @dy-border-default;
    }

    .permission-tree {
      --el-tree-text-color: @dy-text-primary;
      --el-tree-node-hover-bg-color: @dy-bg-hover;
      --el-tree-node-selected-bg-color: rgba(254, 44, 85, 0.1);
      --el-tree-node-selected-text-color: @dy-brand-red;
      background: @dy-bg-elevated;
      padding: 16px;
      border-radius: 8px;
      border: @dy-border-default;
      height: calc(100vh - 180px);
      overflow-y: auto;
    }
  }

  // 弹窗样式
  ::v-deep(.el-dialog) {
    background: @dy-bg-container;
    border: @dy-border-default;
    --el-dialog-text-color: @dy-text-primary;

    .el-form {
      .el-form-item__label {
        color: @dy-text-secondary;
      }

      .el-input {
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