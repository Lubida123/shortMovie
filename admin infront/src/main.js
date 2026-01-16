import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/tokens.css';
import './styles/typography.css';
import './styles/grid.css';
import './styles/animations.css';
// 仅引入管理员路由
import adminRouter from './router/adminRouter'

const app = createApp(App)
app.use(ElementPlus)
app.use(adminRouter) // 只挂载管理员路由
app.mount('#app')