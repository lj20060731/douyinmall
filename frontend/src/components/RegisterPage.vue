<template>
  <div class="register-container">
    <h1>用户注册</h1>
    <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="120px" class="register-form">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="registerForm.password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="registerForm.sex">
          <el-radio label="男">男</el-radio>
          <el-radio label="女">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="身份证号" prop="idNumber">
        <el-input v-model="registerForm.idNumber" placeholder="请输入身份证号"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>
      <el-form-item label="头像" prop="image">
        <el-upload
          class="avatar-uploader"
          action="http://127.0.0.1:9997/common/upload"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload">
          <img v-if="registerForm.image" :src="registerForm.image + '?' + new Date().getTime()" class="avatar">
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-form-item>
      <el-form-item :rules="[...(rules.image || []), { required: true, message: '头像为必填项', trigger: 'blur' }]">
        <!-- 这里添加了规则 -->
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

// 注册表单数据
const registerForm = reactive({
  username: '',
  image: '',
  password: '',
  phone: '',
  sex: '',
  email: '',
  idNumber: ''
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  image: [] 
};

// 获取表单引用
const registerFormRef = ref(null);

// 处理头像上传成功事件
const handleAvatarSuccess = (response, file) => {
  if (response.code === 200 && response.data) {
    // 假设后端返回的 data 字段是图片的 URL
    registerForm.image = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error('头像上传失败');
  }
};

// 头像上传前的处理
const beforeAvatarUpload = (file) => {
  const isImage = file.type.indexOf('image') === 0;
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return false;
  }
  return true;
};

// 提交表单
const submitForm = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      // 根据性别转换为对应的数字
      let sexValue;
      if (registerForm.sex === '男') {
        sexValue = 1;
      } else if (registerForm.sex === '女') {
        sexValue = 0;
      }

      // 创建一个新的表单数据对象，复制 registerForm 的值并替换 sex 字段
      const formData = {
       ...registerForm,
        sex: sexValue
      };
      try{
        axios.post('http://127.0.0.1:9997/user/register', formData)
        .then(response => {
            if (response.status === 200) {
              ElMessage.success('注册成功');
              router.push({ name: 'login' });
            } 
          })
      } catch(error){
        ElMessage.success('注册失败');
      }

    } else {
      ElMessage.error('表单信息填写不完整或有误，请检查');
      return false;
    }
  });
};
</script>

<style scoped>
.register-container {
  width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.register-form {
  margin-top: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  margin-bottom: 10px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style>