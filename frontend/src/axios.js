import axios from 'axios';
import { useRouter } from 'vue-router';

// 获取令牌的函数
const getToken = () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        const confirmed = confirm('未找到令牌，请先登录');
        if (confirmed) {
            const router = useRouter();
            router.push({ path: '/login' });
        }
        return null;
    }
    return token;
};

const createAxiosInstance = () => {
    const token = getToken();
    if (!token) {
        return null;
    }
    const instance = axios.create({
        baseURL: 'http://192.168.163.129:9999', // 后端服务地址
        timeout: 5000,
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    return instance;
};

const axiosInstance = createAxiosInstance();

export default axiosInstance;