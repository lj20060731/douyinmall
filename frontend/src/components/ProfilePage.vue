<template>
  <div>

    <!-- 导航条部分 -->
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane label="商品" name="products"></el-tab-pane>
      <el-tab-pane label="优惠券" name="promotion"></el-tab-pane>
      <el-tab-pane label="购物车" name="cart"></el-tab-pane>
      <el-tab-pane label="我的" name="profile"></el-tab-pane>
      <el-tab-pane label="订单" name="order"></el-tab-pane>
      <el-tab-pane label="退出登录" name="logout"></el-tab-pane>
    </el-tabs>

    <!-- 用户信息展示部分 -->
    <div v-if="userInfo" class="user-info">
      <div class="user-header">
        <img :src="userInfo.image" alt="头像" class="avatar" />
        <span class="username">{{ userInfo.username }}</span>
        <el-button type="primary" @click="showPasswordDialog" style="margin-left: 9px;">修改信息</el-button>
        <el-button type="primary" @click="showAddressDialog" style="margin-left: 9px;">地址管理</el-button>
      </div>
      <div class="info-block password">
        <span>密码:</span>
        <span>******</span>
      </div>
      <div class="info-block phone">
        <span>电话:</span>
        <span>{{ userInfo.phone }}</span>
      </div>
      <div class="info-block sex">
        <span>性别:</span>
        <span>{{ userInfo.sex === 0? '女' : '男' }}</span>
      </div>
      <div class="info-block email">
        <span>邮箱:</span>
        <span>{{ userInfo.email }}</span>
      </div>
      <div class="info-block idNumber">
        <span>身份证号:</span>
        <span>{{ userInfo.idNumber }}</span>
      </div>
      <div class="info-block createTime">
        <span>创建时间:</span>
        <span>{{ formatDate(userInfo.createTime) }}</span>
      </div>
    </div>

    <!-- 密码输入弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="请输入密码" :close-on-click-modal="true" @close="handlePasswordDialogClose">
      <el-input v-model="inputPassword" type="password" placeholder="请输入密码"></el-input>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handlePasswordDialogClose">取消</el-button>
          <el-button type="primary" @click="verifyPassword">确认</el-button>
        </span>
      </template>
    </el-dialog>
    <div v-if="showPasswordOverlay" class="overlay" @click="handlePasswordDialogClose"></div>

    <!-- 修改信息弹窗 -->
    <el-dialog v-model="editInfoDialogVisible" title="修改信息" :close-on-click-modal="true" @close="handleEditInfoDialogClose">
      <el-form ref="editUserInfoFormRef" :model="editUserInfo" label-width="120px" :rules="editUserInfoRules">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editUserInfo.username"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="http://127.0.0.1:9997/common/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="editUserInfo.image" :src="editUserInfo.image + '?' + new Date().getTime()" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="editUserInfo.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editUserInfo.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editUserInfo.sex">
            <el-radio :value="0">女</el-radio>
            <el-radio :value="1">男</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editUserInfo.email"></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="editUserInfo.idNumber"></el-input>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="editUserInfo.createTime" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleEditInfoDialogClose">取消</el-button>
          <el-button type="primary" @click="updateUserInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
    <div v-if="showEditInfoOverlay" class="overlay" @click="handleEditInfoDialogClose"></div>

    <!-- 地址管理弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="地址管理" :close-on-click-modal="true" @close="handleAddressDialogClose">
      <div v-if="addressList.length > 0">
        <h3>已有地址列表</h3>
        <div class="address-list-container">
          <div v-for="(address, index) in addressList" :key="index" class="address-item" :class="{ 'default-address': address.isDefault }">
            <div class="address-info">
              <p>收货人: {{ address.consignee }}</p>
              <p>手机号: {{ address.phone }}</p>
              <p>性别: {{ address.sex === 0? '女' : '男' }}</p>
              <p>地址: {{ address.provinceName }} {{ address.cityName }} {{ address.districtName }} {{ address.detail }}</p>
              <el-button type="danger" @click="deleteAddress(address.id)">
                删除<i class="fa fa-trash" aria-hidden="true"></i>
              </el-button>
            </div>
            <div class="address-actions">
              <el-button type="text" @click="showEditAddressDialog(address)" style="margin-right: 4px;">编辑地址</el-button>
              <el-button v-if="!address.isDefault" type="text" @click="setDefaultAddress(address.id)">设为默认地址</el-button>
              <span v-else class="default-tag">默认地址</span>
            </div>
          </div>
        </div>
      </div>
      <el-button type="primary" @click="showAddAddressDialog" v-if="!isAddingAddress" style="margin-top: 5px;" >新增地址</el-button>

      <!-- 新增地址弹窗 -->
      <el-dialog v-model="addAddressDialogVisible" :title="addAddressDialogTitle" :close-on-click-modal="true" @close="handleCancel">
        <el-form ref="addressFormRef" :model="addressForm" label-width="120px" :rules="addressFormRules">
          <el-form-item label="收货人" prop="consignee">
            <el-input v-model="addressForm.consignee"></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="addressForm.phone"></el-input>
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="addressForm.sex">
              <el-radio :value="0">女</el-radio>
              <el-radio :value="1">男</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="省市区" prop="address">
            <el-cascader
              v-model="addressForm.address"
              :options="addressOptions"
              @change="handleAddressChange"
              placeholder="请选择省市区"
              :props="{ value: 'value', label: 'label', children: 'children' }"
            ></el-cascader>
          </el-form-item>
          <el-form-item label="详细地址" prop="detail">
            <el-input v-model="addressForm.detail"></el-input>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="isEdit? updateAddress() : createAddress()">
          {{ isEdit? '保存修改' : '保存地址' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-dialog>
    </el-dialog>
    <div v-if="showAddressOverlay" class="overlay" @click="handleAddressDialogClose"></div>
    <div v-if="showAddAddressOverlay" class="overlay" @click="handleAddAddressDialogClose"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, computed } from 'vue';
import axios from 'axios';
import type { TabsPaneContext } from 'element-plus';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import areaData from 'china-area-data';
import CryptoJS from 'crypto-js';

// 导航条相关代码
const activeName = ref('');
const router = useRouter();

// 根据当前路由动态设置 activeName
const setActiveName = () => {
  const currentRoute = router.currentRoute.value;
  if (currentRoute.name === 'products') {
    activeName.value = 'products';
  } else if (currentRoute.name === 'cart') {
    activeName.value = 'cart';
  } else if (currentRoute.name === 'profile') {
    activeName.value = 'profile';
  } else if (currentRoute.name === 'order') {
    activeName.value = 'order';
  } else {
    activeName.value = 'promotion';
  }
};

// 初始化时设置 activeName
onMounted(() => {
  setActiveName();
});

// 监听路由变化，更新 activeName
watch(() => router.currentRoute.value, () => {
  setActiveName();
});

const handleClick = (tab: TabsPaneContext, event: Event) => {
  const { name } = tab.props;
  if (name === 'products') {
    router.push({ name: 'products' });
  } else if (name === 'cart') {
    router.push({ name: 'cart' });
  } else if (name === 'profile') {
    router.push({ name: 'profile' });
  } else if (name === 'order') {
    router.push({ name: 'order' });
  } else if (name === 'logout') {
    showLogoutConfirm();
  } else {
    router.push({ name: 'promotion' });
  }
};

const showLogoutConfirm = () => {
  ElMessageBox.confirm('你确定要退出登录吗?', '确认退出', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'custom-message-box'
  })
  .then(() => {
    // 清除 localStorage 数据
    localStorage.clear();
    router.replace({ path: '/login' });
  })
  .catch(() => {
    router.push({ path: '/products' });
  });
};

// 用户信息相关代码
const userInfo = ref<{
  username: string;
  image: string;
  password: string;
  phone: string;
  sex: number;
  email: string;
  idNumber: string;
  createTime: string;
  updateTime: string;
} | null>(null);

// 获取令牌的函数
const getToken = () => {
  const token = localStorage.getItem('jwtToken');
  if (!token) {
    const confirmed = confirm('未找到令牌，请先登录');
    if (confirmed) {
      router.push({ path: '/login' });
    }
    return null;
  }
  return token;
};

// 从后端获取用户信息
const fetchUserInfo = async () => {
  try {
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // 发送请求
    const response = await instance.get('http://127.0.0.1:9997/user/getInfo');
    userInfo.value = response.data.data.user;
    localStorage.setItem('userInfo', JSON.stringify(response.data.data.user));
  } catch (error) {
    ElMessage.error('获取用户信息失败, 请稍后再试');
  } 
};

onMounted(fetchUserInfo);

// 格式化日期时间的函数
const formatDate = (dateTimeString: string) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleString();
};

// 密码输入弹窗相关
const passwordDialogVisible = ref(false);
const inputPassword = ref('');

const showPasswordDialog = () => {
    passwordDialogVisible.value = true;
    showPasswordOverlay.value = true;
};

const verifyPassword = async () => {
    // 对输入的密码进行 MD5 加密
    const encryptedPassword = CryptoJS.MD5(inputPassword.value).toString();
    if (encryptedPassword === userInfo.value.password) {
        passwordDialogVisible.value = false;
        editInfoDialogVisible.value = true;
        showEditInfoOverlay.value = true;
        inputPassword.value = '';
        // 填充修改信息表单
        editUserInfo.value = userInfo.value;
    } else {
        ElMessageBox.alert('密码错误，请重新输入', '提示', {
            confirmButtonText: '确定',
            type: 'error'
        })
        .catch(() => {    });
        inputPassword.value = '';
    }
};

const editUserInfoRules = {
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
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' }
  ]
};

const addressFormRules = {
  consignee: [
    { required: true, message: '请输入收货人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请选择省市区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
};

const editUserInfoFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 修改信息弹窗相关
const editInfoDialogVisible = ref(false);
const editUserInfo = ref<{
  username: string;
  image: string;
  password: string;
  phone: string;
  sex: number;
  email: string;
  idNumber: string;
  createTime: string;
} | null>(null);

const updateUserInfo = async () => {
  try {
    if (!editUserInfoFormRef.value) return;
    const isValid = await editUserInfoFormRef.value.validate();
    if (!isValid) {
      ElMessage.error('请填写完整的用户信息');
      return;
    }
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const response = await instance.post('http://127.0.0.1:9997/user/update', editUserInfo.value);
    if (response.status === 200) {
      ElMessage.success('用户信息更新成功');
      editInfoDialogVisible.value = false;
      // 重新获取地址列表
      await fetchUserInfo();
      if (editUserInfoFormRef.value) {
        editUserInfoFormRef.value.resetFields();
      }
    } else {
      ElMessage.error('修改用户信息失败');
    }
  } catch (error) {
    ElMessage.error('修改用户信息失败，请检查重试');
  }
};

const handleEditUserInfoCancel = () => {
  editInfoDialogVisible.value = false;
  if (editUserInfoFormRef.value) {
    editUserInfoFormRef.value.resetFields();
  }
};

// 头像上传相关
const handleAvatarSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    // 假设后端返回的 data 字段是图片的 URL
    editUserInfo.value!.image = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error('头像上传失败');
  }
};

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.indexOf('image') === 0;
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return false;
  }
  return true;
};

// 地址管理弹窗相关
const addressList = ref<{
  id:number;
  consignee: string;
  phone: string;
  sex: Number;
  provinceName: string;
  cityName: string;
  districtName: string;
  detail: string;
  isDefault: boolean;
}[]>([]);

const addressOptions = ref<any[]>([]);
const isEdit = ref(false);
const editingAddressId = ref<number | null>(null);
  const addAddressDialogTitle = computed(() => {
  return isEdit.value ? '编辑地址' : '新增地址';
});

// 处理省市区数据格式
const formatAddressData = (data: any) => {
  const options = [];
  // 遍历省份数据（第一层数据，键为省份代码）
  for (const provinceCode in data[86]) {
    const provinceName = data[86][provinceCode];
    const provinceOption = {
      value: parseInt(provinceCode),
      label: provinceName,
      children: []
    };
    // 检查该省份下是否有城市数据
    if (data[provinceCode]) {
      // 遍历城市数据（第二层数据，键为城市代码）
      for (const cityCode in data[provinceCode]) {
        const cityName = data[provinceCode][cityCode];
        const cityOption = {
          value: parseInt(cityCode),
          label: cityName,
          children: []
        };
        // 检查该城市下是否有区数据
        if (data[cityCode]) {
          // 遍历区数据（第三层数据，键为区代码）
          for (const districtCode in data[cityCode]) {
            const districtName = data[cityCode][districtCode];
            const districtOption = {
              value: parseInt(districtCode),
              label: districtName
            };
            cityOption.children.push(districtOption);
          }
        }
        provinceOption.children.push(cityOption);
      }
    }
    options.push(provinceOption);
  }
  return options;
};

const isAddingAddress = ref(false);

const showAddressDialog = async () => {
  addressDialogVisible.value = true;
  showAddressOverlay.value = true;
  // 先获取地址列表
  await fetchAddressList();
  // 初始时不显示新增地址表单
  isAddingAddress.value = false;
  if (areaData) {
    addressOptions.value = formatAddressData(areaData);
  } else {
    ElMessage.error('获取地理信息失败');
  }
};
// 地址管理弹窗相关
const addressDialogVisible = ref(false);
const addAddressDialogVisible = ref(false); // 新增地址弹窗的显示状态
const addressForm = ref<{
  consignee: string;
  phone: string;
  sex: number;
  address: number[]; 
  detail: string;
  provinceName: string; 
  cityName: string; 
  districtName: string; 
}>({
  consignee: '',
  phone: '',
  sex: 0,
  address: [],
  detail: '',
  provinceName: '',
  cityName: '',
  districtName: ''
});
const addressFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

const showAddAddressDialog = () => {
  addAddressDialogVisible.value = true;
  showAddAddressOverlay.value = true;
  isEdit.value = false;
  editingAddressId.value = null;
  // 获取用户信息并赋值给 addressForm
  const userInfoStr = localStorage.getItem('userInfo');
  if (userInfoStr) {
    const user = JSON.parse(userInfoStr);
    addressForm.value.consignee = user.username || '';
    addressForm.value.phone = user.phone || '';
    addressForm.value.sex = user.sex;
  } else {
    // 清空表单数据
    addressForm.value = {
      consignee: '',
      phone: '',
      sex: 0,
      address: [],
      detail: '',
      provinceName: '',
      cityName: '',
      districtName: ''
    };
  }
};

const createAddress = async () => {
  try {
    if (!addressFormRef.value) return;
    // 手动触发表单验证
    const isValid = await addressFormRef.value.validate();
    if (!isValid) {
      ElMessage.error('请填写完整的地址信息');
      return;
    }
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // 发送请求
    const response = await instance.post('http://127.0.0.1:9997/address/add', addressForm.value);
    if (response.status === 200) {
      ElMessage.success('地址创建成功');
      addAddressDialogVisible.value = false;
      await fetchAddressList();
      // 重置表单内容
      addressForm.value = {
        consignee: '',
        phone: '',
        sex: 0,
        address: [],
        detail: '',
        provinceName: '',
        cityName: '',
        districtName: ''
      };
    } else {
      ElMessage.error('地址创建失败，请稍后重试');
    }
    if (addressFormRef.value) {
          addressFormRef.value.resetFields();
    }
  } catch (error) {
    ElMessage.error('地址创建失败，请稍后重试');
  }
};

const handleAddressChange = (value: number[]) => {
  let provinceName = '';
  let cityName = '';
  let districtName = '';
  const findAddressName = (options: any[], values: number[]) => {
    for (const option of options) {
      if (option.value === values[0]) {
        provinceName = option.label;
        if (option.children && values.length > 1) {
          for (const child of option.children) {
            if (child.value === values[1]) {
              cityName = child.label;
              if (child.children && values.length > 2) {
                for (const grandChild of child.children) {
                  if (grandChild.value === values[2]) {
                    districtName = grandChild.label;
                    break;
                  }
                }
              }
              break;
            }
          }
        }
        break;
      }
    }
  };
  findAddressName(addressOptions.value, value);
  addressForm.value.provinceName = provinceName;
  addressForm.value.cityName = cityName;
  addressForm.value.districtName = districtName;
};

// 获取地址列表
const fetchAddressList = async () => {
  try {
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // 发送请求
    const response = await instance.get('http://127.0.0.1:9997/address/get');
    if (response.data.code === 200) {
      addressList.value = response.data.data.map((address: any) => ({
        ...address,
        isDefault: address.isDefault || false // 初始化是否为默认地址的标志
      }));
    } else {
      ElMessage.error('获取地址失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error('获取地址失败，请稍后重试');
  }
};

// 设为默认地址的函数
const setDefaultAddress = async (addressId: number) => {
  try {
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // 先取消旧的默认地址
    const oldDefaultAddress = addressList.value.find(address => address.isDefault);
    if (oldDefaultAddress && oldDefaultAddress.id !== addressId) {
      // 发送取消默认地址的请求
      const subDefaultResponse = await instance.post('http://127.0.0.1:9997/address/subdefault', null, {
        params: {
          id: oldDefaultAddress.id
        }
      });
      if (subDefaultResponse.status !== 200) {
        ElMessage.error('取消旧默认地址失败，请稍后重试');
        return;
      }
    }

    // 发送设置新默认地址的请求
    const response = await instance.post('http://127.0.0.1:9997/address/adddefault', null, {
      params: {
        id: addressId
      }
    });
    if (response.status === 200) {
      ElMessage.success('设置默认地址成功');
      await fetchAddressList();
    } else {
      ElMessage.error('设置默认地址失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试');
  }
};

const showEditAddressDialog = (address: any) => {
  addAddressDialogVisible.value = true;
  isEdit.value = true;
  editingAddressId.value = address.id;

  // 查找省市区代码的辅助函数
  const findCode = (options: any[], name: string): number | null => {
    for (const option of options) {
      if (option.label === name) {
        return option.value;
      }
      if (option.children) {
        const childCode = findCode(option.children, name);
        if (childCode!== null) {
          return childCode;
        }
      }
    }
    return null;
  };

  const provinceCode = findCode(addressOptions.value, address.provinceName);
  const cityCode = provinceCode!== null? findCode(addressOptions.value.find((option) => option.value === provinceCode)?.children || [], address.cityName) : null;
  const districtCode = cityCode!== null? findCode(addressOptions.value.find((option) => option.value === provinceCode)?.children?.find((child) => child.value === cityCode)?.children || [], address.districtName) : null;

  addressForm.value = {
    consignee: address.consignee,
    phone: address.phone,
    sex: address.sex,
    address: [provinceCode, cityCode, districtCode].filter((code) => code!== null) as number[],
    detail: address.detail,
    provinceName: address.provinceName,
    cityName: address.cityName,
    districtName: address.districtName
  };
};

const updateAddress = async () => {
  try {
    if (!addressFormRef.value) return;
    // 手动触发表单验证
    const isValid = await addressFormRef.value.validate();
    if (!isValid) {
      ElMessage.error('请填写完整的地址信息');
      return;
    }
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // 添加地址 ID 到请求数据中
    const requestData = {
      id: editingAddressId.value,
      ...addressForm.value
    };
    // 发送请求
    const response = await instance.post('http://127.0.0.1:9997/address/update', requestData);
    if (response.status === 200) {
      ElMessage.success('地址修改成功');
      addAddressDialogVisible.value = false;
      await fetchAddressList();
      // 重置表单内容
      addressForm.value = {
        consignee: '',
        phone: '',
        sex: 0,
        address: [],
        detail: '',
        provinceName: '',
        cityName: '',
        districtName: ''
      };
      isEdit.value = false;
      editingAddressId.value = null;
    } else {
      ElMessage.error('地址修改失败，请稍后重试');
    }
    if (addressFormRef.value) {
          addressFormRef.value.resetFields();
    }
  } catch (error) {
    ElMessage.error('地址修改失败，请稍后重试');
  }
};

const handleCancel = () => {
    addAddressDialogVisible.value = false;
    // 重置表单内容
    addressForm.value = {
        consignee: '',
        phone: '',
        sex: 0,
        address: [],
        detail: '',
        provinceName: '',
        cityName: '',
        districtName: ''
    };
    isEdit.value = false;
    editingAddressId.value = null;
    // 清除表单验证状态
    if (addressFormRef.value) {
        addressFormRef.value.resetFields();
    }
    handleAddAddressDialogClose();
};

const deleteAddress = async (addressId: number) => {
  try {
    // 弹出确认删除窗口
    await ElMessageBox.confirm('确定要删除该地址吗？', '确认删除', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    });
    const token = getToken();
    if (!token) return;
    // 创建一个 axios 实例，并设置请求头
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    // 发送删除请求
    const response = await instance.post('http://127.0.0.1:9997/address/delete', null, {
      params: {
        id: addressId,
      },
    });
    if (response.status === 200) {
      ElMessage.success('地址删除成功');
      // 重新获取地址列表
      await fetchAddressList();
    } else {
      ElMessage.error('地址删除失败，请稍后重试');
    }
  } catch (error) {
    // 用户取消删除操作，直接返回
    return;
  }
};

// 添加遮罩层相关的响应式变量
const showPasswordOverlay = ref(false);
const showEditInfoOverlay = ref(false);
const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);

// 关闭密码弹窗
const handlePasswordDialogClose = () => {
    passwordDialogVisible.value = false;
    showPasswordOverlay.value = false;
};

// 关闭修改信息弹窗
const handleEditInfoDialogClose = () => {
    editInfoDialogVisible.value = false;
    showEditInfoOverlay.value = false;
};

// 关闭地址管理弹窗
const handleAddressDialogClose = () => {
    addressDialogVisible.value = false;
    showAddressOverlay.value = false;
};

// 关闭新增地址弹窗
const handleAddAddressDialogClose = () => {
    addAddressDialogVisible.value = false;
    showAddAddressOverlay.value = false;
};

</script>

<style scoped>
.demo-tabs >.el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}

/* 让标签变大 */
.el-tabs__item {
  font-size: 30px;
  padding: 40px 40px;
}

.user-info {
  margin-top: 20px;
  padding: 30px; /* 增加内边距 */
  border: 1px solid #ddd;
  border-radius: 10px; /* 增加圆角 */
  background-color: #f9f9f9;
  width: 380px; /* 调整整体宽度 */
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); /* 添加立体效果 */
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px; /* 增加下边距 */
}

.avatar {
  width: 70px; /* 增大图片尺寸 */
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 20px; /* 增加右边距 */
}

.username {
  font-size: 22px; /* 增大字体 */
  font-weight: bold;
}

.info-block {
  display: flex;
  align-items: center;
  margin: 10px 0; /* 调整间距 */
  padding: 10px; /* 调整内边距 */
  border-radius: 5px;
}

.info-block span:first-child {
  width: 120px; /* 调整标签宽度 */
  font-weight: bold;
}

.password {
  background-color: #ffe7ba;
}

.phone {
  background-color: #e1f3d8;
}

.sex {
  background-color: #d6e4ff;
}

.email {
  background-color: #f9e5ff;
}

.idNumber {
  background-color: #fff2e8;
}

.createTime {
  background-color: #f5f5f5;
}

.updateTime {
  background-color: #f0f9eb;
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
/* 地址管理弹窗整体样式 */
.el-dialog.address-dialog {
  width: 500px; /* 调整弹窗宽度 */
}

/* 表单标签样式 */
.el-form-item__label {
  font-weight: bold;
}

/* 表单输入框样式 */
.el-input {
  width: 100%;
}

/* 单选框组样式 */
.el-radio-group {
  display: flex;
  align-items: center;
}

.el-radio {
  margin-right: 20px;
}

/* 保存地址按钮样式 */
.el-button[type="primary"] {
  margin-top: 20px;
}

/* 地址列表容器样式 */
.address-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

/* 单个地址项样式 */
.address-item {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 15px;
  width: 100%;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 默认地址样式 */
.default-address {
  border-color: #1890ff;
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.5);
}

/* 默认地址标签样式 */
.default-tag {
  color: #1890ff;
  font-weight: bold;
}

/* 地址信息样式 */
.address-info {
  margin-bottom: 10px;
}

/* 地址操作按钮样式 */
.address-actions {
  text-align: right;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}
</style>