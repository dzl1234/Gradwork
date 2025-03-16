/**
 * 认证相关的JavaScript功能
 */

// 全局变量存储JWT令牌
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser'));

// 检查用户是否已登录
function isLoggedIn() {
    return authToken !== null && authToken !== undefined;
}

// 注册功能
async function register(username,email,password,preferredLanguage) {
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username,
                email,
                password,
                preferredLanguage
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || '注册失败');
        }

        const data = await response.text();
        return { success: true, message: data || '注册成功！' };
    } catch (error) {
        console.error('注册错误:', error);
        return { success: false, message: error.message };
    }
}

// 登录功能
async function login(username, password) {
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || '登录失败');
        }

        const data = await response.json();

        // 登录成功后存储令牌和用户信息
        authToken = data.token;
        currentUser = { id: data.userId, username: data.username };
        localStorage.setItem('authToken', authToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));

        return { success: true, user: currentUser };
    } catch (error) {
        console.error('登录错误:', error);
        return { success: false, message: error.message };
    }
}

// 注销功能 - 修改为创建新的访客身份而非跳转到登录页面
function logout() {
    // 创建新的访客用户
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    window.location.href = '../html/login.html';
}

// 获取当前用户信息
function getCurrentUser() {
    return currentUser;
}

// 为API请求添加授权头
function getAuthHeader() {
    return { 'Authorization': `Bearer ${authToken}` };
}

// 页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const logoutBtn = document.getElementById('logoutBtn');


    // 登录表单提交
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            const result = await login(username, password);

            if (result.success) {
                window.location.href = '../html/chat.html';
            } else {
                alert(result.message);
            }
        });
    }

    // 注册表单提交
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const preferredLanguage = document.getElementById('preferred-language').value;

            const result = await register(username,email,password,preferredLanguage);

            if (result.success) {
                alert('注册成功！请登录');
                window.location.href = '../html/login.html';
            } else {
                alert(result.message);
            }
        });
    }

    // 注销按钮
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            logout();
        });
    }

    // 不再检查保护页面，允许任何人访问所有页面
});
