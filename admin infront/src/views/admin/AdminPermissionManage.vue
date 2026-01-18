<template>
  <div class="admin-permission-manage-container">
    <!-- 角色列表 -->
    <div class="role-list-container">
      <div class="role-header">
        <h3>角色管理</h3>
        <el-button type="primary" :icon="CirclePlus" @click="handleAddRole">新增角色</el-button>
      </div>

      <!-- 表格容器，添加独立的滚动容器 -->
      <div class="table-scroll-wrapper">
        <el-table 
          :data="roleList" 
          border 
          stripe 
          class="role-table"
          v-loading="loading"
          @row-click="handleRoleClick"
          :row-class-name="tableRowClassName"
        >
          <el-table-column prop="roleCode" label="角色编码" min-width="120">
            <template #default="scope">
              <div class="role-code-cell">
                <el-icon><Key /></el-icon>
                <span>{{ scope.row.roleCode }}</span>
                <el-tag v-if="scope.row.roleCode === 'admin'" size="small" class="admin-role-tag">
                  管理员
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="roleName" label="角色名称" min-width="120">
            <template #default="scope">
              <div class="role-name-cell">
                <el-icon><UserFilled /></el-icon>
                <span>{{ scope.row.roleName }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="description" label="角色描述" min-width="180">
            <template #default="scope">
              <div class="role-description-cell">
                <el-icon><Document /></el-icon>
                <span>{{ scope.row.description || '无描述' }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <div class="role-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click.stop="handleEditRole(scope.row)"
                  :icon="EditPen"
                  class="action-btn edit-btn"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click.stop="handleDeleteRole(scope.row.roleCode)"
                  :icon="Delete"
                  class="action-btn delete-btn"
                  :disabled="scope.row.roleCode === 'admin'"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 统计信息 -->
      <div class="role-stats">
        <div class="stat-item">
          <span class="stat-label">总角色数:</span>
          <span class="stat-value">{{ roleList.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">管理员角色:</span>
          <span class="stat-value">{{ adminRoleCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">自定义角色:</span>
          <span class="stat-value">{{ customRoleCount }}</span>
        </div>
      </div>
    </div>

    <!-- 权限配置区域 -->
    <div class="permission-config-container" v-if="selectedRole">
      <div class="permission-header">
        <div class="permission-title">
          <h3>权限配置 - {{ selectedRole.roleName }}</h3>
          <el-tag class="admin-role-tag-header" size="small" :data-role-type="selectedRole.roleCode === 'admin' ? 'admin' : 'custom'">
            {{ selectedRole.roleCode === 'admin' ? '系统管理员' : '自定义角色' }}
          </el-tag>
        </div>
        <div class="permission-actions">
          <el-button type="primary" @click="handleSavePermission" :loading="savingPermission" :icon="Check">
            保存权限
          </el-button>
          <el-button @click="resetPermissionSelection" :icon="Refresh">重置选择</el-button>
        </div>
      </div>
      
      <!-- 权限统计 -->
      <div class="permission-stats">
        <div class="permission-stat-card">
          <div class="permission-stat-icon" style="background-color: rgba(64, 158, 255, 0.1);">
            <el-icon color="#409EFF"><Collection /></el-icon>
          </div>
          <div class="permission-stat-content">
            <div class="permission-stat-number">{{ totalPermissionCount }}</div>
            <div class="permission-stat-label">总权限数</div>
          </div>
        </div>
        <div class="permission-stat-card">
          <div class="permission-stat-icon" style="background-color: rgba(103, 194, 58, 0.1);">
            <el-icon color="#67C23A"><Check /></el-icon>
          </div>
          <div class="permission-stat-content">
            <div class="permission-stat-number">{{ selectedPermissionCount }}</div>
            <div class="permission-stat-label">已选权限</div>
          </div>
        </div>
        <div class="permission-stat-card">
          <div class="permission-stat-icon" style="background-color: rgba(230, 162, 60, 0.1);">
            <el-icon color="#E6A23C"><Warning /></el-icon>
          </div>
          <div class="permission-stat-content">
            <div class="permission-stat-number">{{ categoryCount }}</div>
            <div class="stat-label">权限分类</div>
          </div>
        </div>
      </div>

      <!-- 权限搜索 -->
      <div class="permission-search">
        <el-input
          v-model="permissionSearch"
          placeholder="搜索权限名称或编码"
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="quick-actions">
          <el-button size="small" @click="selectAllPermissions" :icon="Select">全选</el-button>
          <el-button size="small" @click="clearAllPermissions" :icon="CircleClose">清空</el-button>
        </div>
      </div>

      <!-- 权限树形结构 -->
      <div class="permission-tree-container">
        <el-tree
          ref="permissionTreeRef"
          :data="filteredPermissionTree"
          show-checkbox
          node-key="permissionCode"
          :props="treeProps"
          :default-checked-keys="selectedPermissions"
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          class="permission-tree"
          highlight-current
          @check="handlePermissionCheck"
        >
          <template #default="{ node, data }">
            <div class="tree-node-content">
              <div class="tree-node-main">
                <span class="permission-name">{{ data.permissionName }}</span>
                <span class="permission-code">{{ data.permissionCode }}</span>
              </div>
              <div class="tree-node-description" v-if="data.description">
                {{ data.description }}
              </div>
            </div>
          </template>
        </el-tree>
        
        <!-- 权限树统计 -->
        <div class="tree-stats">
          <div class="tree-stat-item">
            <el-icon><Folder /></el-icon>
            <span>权限模块: {{ categoryCount }}</span>
          </div>
          <div class="tree-stat-item">
            <el-icon><Document /></el-icon>
            <span>权限项: {{ leafPermissionCount }}</span>
          </div>
          <div class="tree-stat-item">
            <el-icon><Check /></el-icon>
            <span>已选: {{ selectedPermissionCount }}/{{ totalPermissionCount }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 权限配置提示（无选中角色时） -->
    <div class="permission-empty-container" v-else>
      <div class="empty-content">
        <el-icon class="empty-icon"><UserFilled /></el-icon>
        <h3>请选择角色</h3>
        <p>在左侧角色列表中选择一个角色，然后配置其权限</p>
        <p class="empty-tip">提示：管理员角色（admin）默认拥有所有权限</p>
      </div>
    </div>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog 
      v-model="roleDialogVisible" 
      :title="isEditRole ? '编辑角色' : '新增角色'" 
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        label-width="100px"
        class="role-form"
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input 
            v-model="roleForm.roleCode" 
            placeholder="请输入角色编码（如：editor）"
            :disabled="isEditRole"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip" v-if="!isEditRole">创建后不可修改，建议使用英文和数字</div>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称">
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            placeholder="请输入角色描述"
            :rows="4"
            show-word-limit
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRole" :loading="savingRole">
            {{ isEditRole ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  CirclePlus, 
  Delete, 
  Search, 
  Collection, 
  Check, 
  Warning, 
  Folder, 
  Document, 
  UserFilled, 
  Key,
  EditPen,
  Refresh,
  Select,
  CircleClose
} from '@element-plus/icons-vue'
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
const savingPermission = ref(false)
const savingRole = ref(false)

// 角色列表 - 只显示管理员和自定义角色，移除普通用户
const roleList = ref([
  {
    roleCode: 'admin',
    roleName: '管理员',
    description: '拥有所有权限'
  },
  {
    roleCode: 'editor',
    roleName: '编辑',
    description: '可以编辑视频和内容'
  }
])
// 选中的角色
const selectedRole = ref(null)

// 权限相关数据
const permissionTree = ref([])
const permissionSearch = ref('')
const selectedPermissions = ref([])
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

// 权限搜索过滤
const filteredPermissionTree = computed(() => {
  if (!permissionSearch.value) {
    return permissionTree.value
  }
  
  const filterNodes = (nodes, searchText) => {
    return nodes.filter(node => {
      const matches = node.permissionName.includes(searchText) || 
                     node.permissionCode.includes(searchText)
      
      if (node.children && node.children.length > 0) {
        const filteredChildren = filterNodes(node.children, searchText)
        if (filteredChildren.length > 0) {
          node.children = filteredChildren
          return true
        }
      }
      
      return matches
    })
  }
  
  return filterNodes([...permissionTree.value], permissionSearch.value)
})

// 权限统计
const totalPermissionCount = computed(() => {
  const countPermissions = (nodes) => {
    return nodes.reduce((total, node) => {
      if (node.children && node.children.length > 0) {
        return total + countPermissions(node.children)
      }
      return total + 1
    }, 0)
  }
  
  return countPermissions(permissionTree.value)
})

const selectedPermissionCount = computed(() => {
  return selectedPermissions.value.length
})

const categoryCount = computed(() => {
  return permissionTree.value.length
})

const leafPermissionCount = computed(() => {
  const countLeafNodes = (nodes) => {
    return nodes.reduce((total, node) => {
      if (node.children && node.children.length > 0) {
        return total + countLeafNodes(node.children)
      }
      return total + 1
    }, 0)
  }
  
  return countLeafNodes(permissionTree.value)
})

// 角色统计 - 只统计管理员和自定义角色
const adminRoleCount = computed(() => {
  return roleList.value.filter(role => role.roleCode === 'admin').length
})

const customRoleCount = computed(() => {
  return roleList.value.filter(role => role.roleCode !== 'admin').length
})

// 角色表单校验规则
const roleFormRules = ref({
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '角色编码仅支持字母、数字、下划线', trigger: 'blur' },
    { min: 2, max: 20, message: '角色编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '角色描述长度不超过 200 个字符', trigger: 'blur' }
  ]
})

// 权限树节点过滤方法
const filterNode = (value, data) => {
  if (!value) return true
  return data.permissionName.includes(value) || data.permissionCode.includes(value)
}

// 获取角色列表 - 过滤掉普通用户角色
const getRoleListFn = async () => {
  try {
    loading.value = true
    const res = await getRoleList()
    // 过滤掉普通用户角色 (roleCode 为 'user')
    roleList.value = res.filter(role => role.roleCode !== 'user')
    
    // 默认选中第一个角色
    if (roleList.value.length > 0 && !selectedRole.value) {
      selectedRole.value = roleList.value[0]
      getRolePermissionFn(roleList.value[0].roleCode)
    }
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
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
      }
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
    description: row.description || ''
  }
  roleDialogVisible.value = true
}

// 保存角色（新增/编辑）
const handleSaveRole = async () => {
  try {
    await roleFormRef.value.validate()
    savingRole.value = true
    
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
  } finally {
    savingRole.value = false
  }
}

// 删除角色
const handleDeleteRole = async (roleCode) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该角色吗？删除后相关用户的权限将受到影响。',
      '警告',
      {
        confirmButtonText: '确定删除',
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

// 权限选中处理
const handlePermissionCheck = (data, checkedInfo) => {
  const { checkedKeys, halfCheckedKeys } = checkedInfo
  selectedPermissions.value = [...checkedKeys, ...halfCheckedKeys]
}

// 保存角色权限
const handleSavePermission = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请先选择角色')
    return
  }
  
  try {
    savingPermission.value = true
    
    // 获取选中的权限节点
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
    
    await saveRolePermission({
      roleCode: selectedRole.value.roleCode,
      permissionCodes: allCheckedKeys
    })
    
    ElMessage.success('保存权限成功')
    selectedPermissions.value = allCheckedKeys
  } catch (error) {
    ElMessage.error('保存权限失败')
  } finally {
    savingPermission.value = false
  }
}

// 重置权限选择
const resetPermissionSelection = () => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys([])
    selectedPermissions.value = []
  }
}

// 全选权限
const selectAllPermissions = () => {
  if (permissionTreeRef.value) {
    const allKeys = getAllPermissionKeys(permissionTree.value)
    permissionTreeRef.value.setCheckedKeys(allKeys)
    selectedPermissions.value = allKeys
  }
}

// 清空所有权限
const clearAllPermissions = () => {
  resetPermissionSelection()
}

// 获取所有权限键
const getAllPermissionKeys = (nodes) => {
  const keys = []
  
  const traverse = (node) => {
    if (node.permissionCode) {
      keys.push(node.permissionCode)
    }
    
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => traverse(child))
    }
  }
  
  nodes.forEach(node => traverse(node))
  return keys
}

// 监听权限搜索
watch(permissionSearch, (val) => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.filter(val)
  }
})

// 初始化
onMounted(() => {
  // 默认选中第一个角色
  if (roleList.value.length > 0) {
    selectedRole.value = roleList.value[0]
  }
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
@dy-text-tertiary: rgba(255, 255, 255, 0.55);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);
@dy-brand-red: #FE2C55;
@dy-brand-cyan: #25F4EE;

// 标签颜色变量（增强对比度）
@tag-admin-bg: rgba(254, 44, 85, 0.3);
@tag-admin-text: #FF6B8B;
@tag-admin-border: rgba(254, 44, 85, 0.5);
@tag-custom-bg: rgba(64, 158, 255, 0.3);
@tag-custom-text: #80C5FF;
@tag-custom-border: rgba(64, 158, 255, 0.5);

.admin-permission-manage-container {
  min-height: calc(100vh - 80px);
  background: @dy-bg-body;
  padding: 20px;
  color: @dy-text-primary;
  display: grid;
  grid-template-columns: 520px 1fr;
  gap: 20px;
  height: calc(100vh - 40px);
  overflow: hidden;

  // 角色列表区域（左侧）
  .role-list-container {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;

    .role-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: @dy-border-default;
      flex-shrink: 0;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: @dy-brand-cyan;
        margin: 0;
      }
    }

    // 表格滚动包装器 - 修复滚动问题
    .table-scroll-wrapper {
      flex: 1;
      overflow: auto;
      margin-bottom: 16px;
      min-height: 0; // 重要：确保flex容器可以正确分配空间
      
      &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: @dy-bg-container;
      }
      
      &::-webkit-scrollbar-thumb {
        background: @dy-text-tertiary;
        border-radius: 3px;
        transition: background 0.2s ease;
        
        &:hover {
          background: @dy-text-secondary;
        }
      }
    }

    .role-table {
      width: 100%;
      min-width: 480px; // 确保表格有最小宽度，避免压缩
      --el-table-text-color: @dy-text-primary;
      --el-table-row-hover-bg-color: rgba(37, 244, 238, 0.08);
      --el-table-header-text-color: @dy-text-secondary;
      --el-table-border-color: rgba(255, 255, 255, 0.08);
      font-size: 13px;

      ::v-deep(.el-table__header-wrapper) {
        th {
          background: @dy-bg-elevated !important;
          font-weight: 600;
          font-size: 14px;
          padding: 14px 8px !important;
          border-color: rgba(255, 255, 255, 0.08) !important;
          position: sticky;
          top: 0;
          z-index: 1;
          
          .cell {
            color: @dy-text-secondary !important;
            font-weight: 600;
            padding: 0 4px;
            white-space: nowrap;
          }
        }
      }

      ::v-deep(.el-table__body-wrapper) {
        overflow-x: auto;
        
        tr {
          cursor: pointer;
          transition: background-color 0.2s ease;
          
          &:hover {
            .el-table__cell {
              background: rgba(37, 244, 238, 0.08) !important;
            }
          }
          
          &.selected-row {
            .el-table__cell {
              background: rgba(254, 44, 85, 0.15) !important;
              border-left: 3px solid @dy-brand-red;
              
              .cell, span {
                color: @dy-brand-red !important;
                font-weight: 500;
              }
            }
          }
        }
        
        .el-table__cell {
          padding: 12px 8px !important;
          border-color: rgba(255, 255, 255, 0.08) !important;
          white-space: nowrap;
        }
      }
      
      // 角色编码单元格
      .role-code-cell {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: @dy-brand-cyan;
          font-size: 14px;
        }
        
        span {
          font-family: monospace;
          font-weight: 500;
        }
        
        // 管理员角色标签样式（增强对比度）
        .admin-role-tag {
          margin-left: 4px;
          height: 22px;
          line-height: 20px;
          font-size: 11px;
          font-weight: 600;
          background: @tag-admin-bg !important;
          color: @tag-admin-text !important;
          border: 1px solid @tag-admin-border !important;
          border-radius: 4px;
          padding: 0 6px;
          transition: all 0.2s ease;
          
          &:hover {
            background: rgba(254, 44, 85, 0.4) !important;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(254, 44, 85, 0.2);
          }
        }
      }
      
      // 角色名称单元格
      .role-name-cell {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: #409EFF;
          font-size: 14px;
        }
        
        span {
          font-weight: 500;
        }
      }
      
      // 角色描述单元格
      .role-description-cell {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: #E6A23C;
          font-size: 14px;
        }
        
        span {
          color: @dy-text-tertiary;
          font-size: 12px;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          text-overflow: ellipsis;
          min-width: 150px;
        }
      }
      
      // 操作列样式
      .role-actions {
        display: flex;
        gap: 8px;
        justify-content: center;
        align-items: center;
        width: 100%;
        
        .action-btn {
          min-width: 68px;
          height: 28px;
          padding: 0 12px;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
          
          &.edit-btn {
            background: rgba(64, 158, 255, 0.1) !important;
            border-color: rgba(64, 158, 255, 0.3) !important;
            color: #409EFF !important;
            
            &:hover {
              background: rgba(64, 158, 255, 0.2) !important;
            }
          }
          
          &.delete-btn {
            background: rgba(245, 108, 108, 0.1) !important;
            border-color: rgba(245, 108, 108, 0.3) !important;
            color: #F56C6C !important;
            
            &:hover:not(:disabled) {
              background: rgba(245, 108, 108, 0.2) !important;
            }
            
            &:disabled {
              opacity: 0.5;
              cursor: not-allowed !important;
            }
          }
        }
      }
    }
    
    // 角色统计
    .role-stats {
      display: flex;
      justify-content: space-between;
      padding: 16px;
      background: @dy-bg-elevated;
      border-radius: 8px;
      border: @dy-border-default;
      margin-top: auto;
      flex-shrink: 0;
      
      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        
        .stat-label {
          font-size: 12px;
          color: @dy-text-tertiary;
        }
        
        .stat-value {
          font-size: 18px;
          font-weight: 600;
          color: @dy-brand-cyan;
          font-family: "DIN Condensed", sans-serif;
        }
      }
    }
  }

  // 权限配置区域（右侧）
  .permission-config-container {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;

    .permission-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: @dy-border-default;
      flex-shrink: 0;
      
      .permission-title {
        display: flex;
        align-items: center;
        gap: 12px;
        
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: @dy-text-primary;
          margin: 0;
        }
        
        // 权限配置标题旁边的标签样式（增强对比度）
        .admin-role-tag-header {
          font-size: 12px;
          height: 24px;
          line-height: 22px;
          font-weight: 600;
          border: none !important;
          
          &[data-role-type="admin"] {
            background: @tag-admin-bg !important;
            color: @tag-admin-text !important;
            border: 1px solid @tag-admin-border !important;
          }
          
          &[data-role-type="custom"] {
            background: @tag-custom-bg !important;
            color: @tag-custom-text !important;
            border: 1px solid @tag-custom-border !important;
          }
          
          &:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            transition: all 0.2s ease;
          }
        }
      }
      
      .permission-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    // 权限统计卡片
    .permission-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      margin-bottom: 20px;
      flex-shrink: 0;
      
      .permission-stat-card {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        border-radius: 8px;
        padding: 16px;
        display: flex;
        align-items: center;
        gap: 12px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
          border-color: rgba(37, 244, 238, 0.3);
        }
        
        .permission-stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .el-icon {
            font-size: 24px;
          }
        }
        
        .permission-stat-content {
          .permission-stat-number {
            font-size: 20px;
            font-weight: 600;
            color: @dy-text-primary;
            line-height: 1.2;
            font-family: "DIN Condensed", sans-serif;
          }
          
          .stat-label {
            font-size: 13px;
            color: @dy-text-tertiary;
            margin-top: 4px;
          }
        }
      }
    }
    
    // 权限搜索
    .permission-search {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      flex-shrink: 0;
      
      .search-input {
        flex: 1;
        
        :deep(.el-input__wrapper) {
          background: @dy-bg-elevated;
          border: @dy-border-default;
          box-shadow: none;
          
          &:hover {
            border-color: rgba(37, 244, 238, 0.3);
          }
        }
      }
      
      .quick-actions {
        display: flex;
        gap: 8px;
        
        .el-button {
          min-width: 60px;
        }
      }
    }
    
    // 权限树容器
    .permission-tree-container {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: @dy-bg-elevated;
      border: @dy-border-default;
      border-radius: 8px;
      overflow: hidden;
      min-height: 0; // 重要：确保flex容器可以正确分配空间
      
      .permission-tree {
        flex: 1;
        padding: 16px;
        overflow-y: auto;
        
        ::v-deep(.el-tree-node) {
          margin-bottom: 8px;
          
          &:hover {
            background: rgba(37, 244, 238, 0.05);
            border-radius: 4px;
          }
          
          &.is-current {
            > .el-tree-node__content {
              background: rgba(254, 44, 85, 0.1);
              border-radius: 4px;
            }
          }
          
          .el-tree-node__content {
            height: 40px;
            padding: 0 8px;
            
            &:hover {
              background: transparent;
            }
          }
        }
        
        .tree-node-content {
          width: 100%;
          
          .tree-node-main {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .permission-name {
              font-size: 14px;
              color: @dy-text-primary;
              font-weight: 500;
            }
            
            .permission-code {
              font-size: 12px;
              color: @dy-brand-cyan;
              background: rgba(37, 244, 238, 0.1);
              padding: 2px 6px;
              border-radius: 4px;
              font-family: monospace;
            }
          }
          
          .tree-node-description {
            font-size: 12px;
            color: @dy-text-tertiary;
            margin-top: 4px;
            line-height: 1.4;
          }
        }
      }
      
      // 树统计
      .tree-stats {
        display: flex;
        justify-content: space-between;
        padding: 12px 16px;
        background: rgba(0, 0, 0, 0.2);
        border-top: @dy-border-default;
        flex-shrink: 0;
        
        .tree-stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: @dy-text-tertiary;
          
          .el-icon {
            font-size: 14px;
          }
          
          &:nth-child(1) .el-icon {
            color: #409EFF;
          }
          
          &:nth-child(2) .el-icon {
            color: #67C23A;
          }
          
          &:nth-child(3) .el-icon {
            color: #E6A23C;
          }
        }
      }
    }
  }
  
  // 无选中角色时的提示
  .permission-empty-container {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    
    .empty-content {
      text-align: center;
      max-width: 400px;
      
      .empty-icon {
        font-size: 64px;
        color: @dy-brand-cyan;
        margin-bottom: 20px;
        opacity: 0.5;
      }
      
      h3 {
        font-size: 20px;
        color: @dy-text-primary;
        margin-bottom: 12px;
        font-weight: 600;
      }
      
      p {
        color: @dy-text-tertiary;
        margin-bottom: 8px;
        line-height: 1.6;
      }
      
      .empty-tip {
        font-size: 13px;
        color: @dy-brand-red;
        margin-top: 16px;
        padding: 8px 12px;
        background: rgba(254, 44, 85, 0.1);
        border-radius: 6px;
        display: inline-block;
      }
    }
  }

  // 弹窗样式
  ::v-deep(.el-dialog) {
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
    
    .role-form {
      .el-form-item {
        margin-bottom: 20px;
        
        .el-form-item__label {
          color: @dy-text-secondary;
          font-weight: 500;
          padding-right: 16px;
        }
        
        .el-input, .el-textarea {
          background: @dy-bg-elevated;
          border: @dy-border-default;
          border-radius: 6px;
          
          ::v-deep(.el-input__wrapper) {
            background: transparent;
            box-shadow: none;
            border: none;
          }
          
          ::v-deep(.el-textarea__inner) {
            background: transparent;
            border: none;
            color: @dy-text-primary;
            resize: none;
          }
          
          &:hover {
            border-color: rgba(37, 244, 238, 0.3);
          }
          
          &:focus-within {
            border-color: @dy-brand-cyan;
            box-shadow: 0 0 0 1px rgba(37, 244, 238, 0.2);
          }
        }
        
        .form-tip {
          font-size: 12px;
          color: @dy-text-tertiary;
          margin-top: 4px;
        }
      }
    }
    
    .el-dialog__footer {
      border-top: @dy-border-default;
      padding-top: 16px;
      margin-top: 20px;
      
      .dialog-footer {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
    }
  }
}

// 响应式适配
@media (max-width: 1200px) {
  .admin-permission-manage-container {
    grid-template-columns: 480px 1fr;
    
    .role-actions {
      flex-direction: column;
      gap: 6px;
      
      .action-btn {
        width: 100%;
        min-width: 100%;
      }
    }
  }
}

@media (max-width: 992px) {
  .admin-permission-manage-container {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    height: auto;
    overflow: auto;
    
    .role-list-container,
    .permission-config-container,
    .permission-empty-container {
      height: 500px;
    }
    
    .role-actions {
      flex-direction: row;
      gap: 8px;
      
      .action-btn {
        width: auto;
        min-width: 68px;
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-permission-manage-container {
    padding: 12px;
    gap: 12px;
    
    .permission-stats {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .role-list-container,
    .permission-config-container,
    .permission-empty-container {
      height: 450px;
    }
    
    .role-actions {
      flex-direction: column;
      gap: 6px;
      
      .action-btn {
        width: 100%;
        min-width: 100%;
      }
    }
    
    // 移动端表格滚动优化
    .table-scroll-wrapper {
      min-width: 0;
      
      .role-table {
        min-width: 0;
        
        ::v-deep(.el-table__body-wrapper) {
          overflow-x: auto;
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .admin-permission-manage-container {
    .permission-stats {
      grid-template-columns: 1fr;
    }
    
    .permission-search {
      flex-direction: column;
      
      .quick-actions {
        justify-content: flex-end;
      }
    }
  }
}
</style>