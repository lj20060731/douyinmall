<template>
  <div class="login-container">
    <div class="login-form">
      <h2>登录</h2>
      <form @submit.prevent="submitLoginForm">
        <div class="input-group">
          <FontAwesomeIcon icon="fa-solid fa-user" class="input-icon" />
          <input v-model="username" type="text" name="username" class="form-control" placeholder="用户名" required/>
        </div>
        <div class="input-group">
          <FontAwesomeIcon icon="fa-solid fa-lock" class="input-icon" />
          <input v-model="password" type="password" name="password" class="form-control" placeholder="密码" required/>
        </div>
        <div class="input-group">
          <FontAwesomeIcon icon="fa-solid fa-envelope" class="input-icon" />
          <input v-model="email" type="email" name="email" class="form-control" placeholder="邮箱" required/>
        </div>
        <button type="submit" class="btn-login">登录</button>
        <div class="register-link">
          还没有账号？<a @click="navigateToRegisterPage">前往注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faUser, faLock, faEnvelope } from '@fortawesome/free-solid-svg-icons';
import { ElMessage } from 'element-plus';
// 添加需要的图标到库中
library.add(faUser, faLock, faEnvelope);

export default {
  name: 'LoginPage',
  components: {
    FontAwesomeIcon // 注册 FontAwesomeIcon 组件
  },
  setup() {
    const username = ref('');
    const password = ref('');
    const email = ref('');
    const router = useRouter();

    const submitLoginForm = async () => {
        const response = await axios.post('http://127.0.0.1:9997/user/login', {
          username: username.value,
          password: password.value,
          email: email.value
        });

        const data = response.data.data;
        if (data.token && response.data.code=='200') {
          localStorage.setItem('jwtToken', data.token);
          console.log(localStorage);
          router.push({ name: 'index' });
        } else {
          ElMessage.error('账号或密码错误');
        }
    };

    const navigateToRegisterPage = () => {
      router.push({ name: 'register' });
    };

    return {
      username,
      password,
      email,
      submitLoginForm,
      navigateToRegisterPage
    };
  }
};
</script>

<style scoped>
.login-container {
  position: absolute;
  top: 80px; /* 距离顶部20px */
  right: 40px; /* 距离右边20px */
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 400px;
}

.login-form h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.input-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 5px 10px;
}

.input-icon {
  margin-right: 10px;
  color: #666;
}

.form-control {
  border: none;
  outline: none;
  flex: 1;
  font-size: 16px;
}

.btn-login {
  width: 100%;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  padding: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.btn-login:hover {
  background-color: #0056b3;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-link a {
  cursor: pointer;
  color: #007bff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>