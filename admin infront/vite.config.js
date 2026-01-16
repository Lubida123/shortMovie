import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 新增：引入path模块处理路径

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 新增：路径别名配置（核心解决@找不到的问题）
  resolve: {
    alias: {
      // 将@指向项目根目录下的src文件夹
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      // 原有：通用/api代理（保留，兼容用户端接口）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // 新增：管理员专属/api/admin代理（可选，与用户端接口隔离）
      '/api/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // 可选：如果后端接口没有/admin前缀，可通过rewrite去掉
        // rewrite: (path) => path.replace(/^\/api\/admin/, '/admin')
      }
    },
    // 可选：配置端口，避免冲突
    port: 5173,
    open: true // 启动时自动打开浏览器
  }
})