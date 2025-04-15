/**
 * 好友管理相关的JavaScript功能
 */

// 加载好友列表
async function loadFriendsList() {
    try {
        const response = await fetch('/api/friends', {
            headers: {
                ...getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error('加载好友列表失败');
        }

        const friends = await response.json();
        return friends;
    } catch (error) {
        console.error('加载好友列表错误:', error);
        showNotification('错误', '无法加载好友列表', 'error');
        return [];
    }
}

// 添加好友
async function addFriend(username) {
    try {
        const response = await fetch(`/api/friends/addByUsername/${encodeURIComponent(username)}`,{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeader()
            },
            body: JSON.stringify({ username })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '添加好友失败');
        }

        return { success: true, message: '好友添加成功' };
    } catch (error) {
        console.error('添加好友错误:', error);
        return { success: false, message: error.message };
    }
}

// 显示添加好友模态框
function showAddFriendModal() {
    const modal = document.getElementById('addFriendModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// 关闭添加好友模态框
function closeAddFriendModal() {
    const modal = document.getElementById('addFriendModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// 初始化添加好友功能
document.addEventListener('DOMContentLoaded', function() {
    // 打开添加好友模态框按钮
    const addFriendBtn = document.getElementById('addFriendBtn');
    if (addFriendBtn) {
        addFriendBtn.addEventListener('click', showAddFriendModal);
    }

    // 关闭按钮
    const closeAddFriendModalBtn = document.querySelector('.close');
    if (closeAddFriendModalBtn) {
        closeAddFriendModalBtn.addEventListener('click', closeAddFriendModal);
    }

    // 添加好友按钮
    const confirmAddFriendBtn = document.getElementById('confirmAddFriendBtn');
    if (confirmAddFriendBtn) {
        confirmAddFriendBtn.addEventListener('click', async function() {
            const addFriendInput = document.getElementById('addFriendInput');
            const username = addFriendInput.value.trim();

            if (username === '') {
                showNotification('提示', '请输入好友用户名', 'info');
                return;
            }

            const result = await addFriend(username);

            if (result.success) {
                showNotification('成功', result.message, 'success');
                addFriendInput.value = '';
                closeAddFriendModal();
                loadFriendsList();
            } else {
                showNotification('错误', result.message, 'error');
            }
        });
    }
});

// 删除好友
async function removeFriend(friendId) {
    try {
        const response = await fetch(`/api/friends/${friendId}`, {
            method: 'DELETE',
            headers: {
                ...getAuthHeader()
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '删除好友失败');
        }

        return { success: true, message: '好友删除成功' };
    } catch (error) {
        console.error('删除好友错误:', error);
        return { success: false, message: error.message };
    }
}

// 搜索用户
async function searchUsers(keyword) {
    try {
        const response = await fetch(`/api/users/search?keyword=${encodeURIComponent(keyword)}`, {
            headers: {
                ...getAuthHeader()
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '搜索用户失败');
        }

        const users = await response.json();
        return users;
    } catch (error) {
        console.error('搜索用户错误:', error);
        showNotification('错误', error.message, 'error');
        return [];
    }
}

// 显示好友管理模态框
function showFriendsModal() {
    const modal = document.getElementById('friendsModal');
    if (modal) {
        modal.style.display = 'block';
        loadFriendsForModal();
    }
}

// 关闭好友管理模态框
function closeFriendsModal() {
    const modal = document.getElementById('friendsModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// 加载好友列表到模态框
async function loadFriendsForModal() {
    const friendsList = document.getElementById('modalFriendsList');
    if (!friendsList) return;

    friendsList.innerHTML = '<div class="loading">正在加载好友列表...</div>';

    const friends = await loadFriendsList();

    if (friends.length === 0) {
        friendsList.innerHTML = '<div class="no-friends">暂无好友</div>';
        return;
    }

    friendsList.innerHTML = '';

    friends.forEach(friend => {
        const friendItem = document.createElement('div');
        friendItem.classList.add('modal-friend-item');

        friendItem.innerHTML = `
            <div class="friend-info">
                <div class="friend-name">${friend.username}</div>
                <div class="friend-email">${friend.email}</div>
                <div class="friend-language">首选语言: ${friend.preferredLanguage}</div>
                <div class="friend-status ${friend.online ? 'online' : 'offline'}">${friend.online ? '在线' : '离线'}</div>
            </div>
            <div class="friend-actions">
                <button class="btn btn-danger remove-friend" data-id="${friend.id}">删除</button>
            </div>
        `;

        friendsList.appendChild(friendItem);
    });

    // 添加删除好友事件监听
    document.querySelectorAll('.remove-friend').forEach(button => {
        button.addEventListener('click', async function() {
            const friendId = this.getAttribute('data-id');
            const result = await removeFriend(friendId);

            if (result.success) {
                showNotification('成功', result.message, 'success');
                loadFriendsForModal(); // 重新加载列表
            } else {
                showNotification('错误', result.message, 'error');
            }
        });
    });
}

// 搜索并添加好友
async function searchAndAddFriend() {
    const searchInput = document.getElementById('searchUserInput');
    if (!searchInput) return;

    const keyword = searchInput.value.trim();

    if (keyword === '') {
        showNotification('提示', '请输入搜索关键词', 'info');
        return;
    }

    const searchResults = document.getElementById('searchResults');
    searchResults.innerHTML = '<div class="loading">正在搜索用户...</div>';

    const users = await searchUsers(keyword);

    if (users.length === 0) {
        searchResults.innerHTML = '<div class="no-results">未找到匹配的用户</div>';
        return;
    }

    searchResults.innerHTML = '';

    users.forEach(user => {
        const userItem = document.createElement('div');
        userItem.classList.add('search-result-item');

        userItem.innerHTML = `
            <div class="user-info">
                <div class="user-name">${user.username}</div>
                <div class="user-language">首选语言: ${user.preferredLanguage}</div>
            </div>
            <div class="user-actions">
                <button class="btn btn-primary add-friend-btn" data-username="${user.username}">添加好友</button>
            </div>
        `;

        searchResults.appendChild(userItem);
    });

    // 添加好友按钮事件
    document.querySelectorAll('.add-friend-btn').forEach(button => {
        button.addEventListener('click', async function() {
            const username = this.getAttribute('data-username');
            const result = await addFriend(username);

            if (result.success) {
                showNotification('成功', result.message, 'success');
                searchInput.value = '';
                searchResults.innerHTML = '';
                loadFriendsForModal(); // 重新加载好友列表
            } else {
                showNotification('错误', result.message, 'error');
            }
        });
    });
}

// 初始化好友管理功能
document.addEventListener('DOMContentLoaded', function() {
    // 打开好友管理按钮
    const openFriendsModalBtn = document.getElementById('manageFriendsBtn');
    if (openFriendsModalBtn) {
        openFriendsModalBtn.addEventListener('click', showFriendsModal);
    }

    // 关闭按钮
    const closeFriendsModalBtn = document.getElementById('closeFriendsModal');
    if (closeFriendsModalBtn) {
        closeFriendsModalBtn.addEventListener('click', closeFriendsModal);
    }

    // 搜索用户按钮
    const searchUserBtn = document.getElementById('searchUserBtn');
    if (searchUserBtn) {
        searchUserBtn.addEventListener('click', searchAndAddFriend);
    }

    // 搜索输入回车事件
    const searchUserInput = document.getElementById('searchUserInput');
    if (searchUserInput) {
        searchUserInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchAndAddFriend();
            }
        });
    }
});

// 获取认证头
function getAuthHeader() {
    return { 'Authorization': `Bearer ${authToken}` };
}

// 检查用户是否已登录
function isLoggedIn() {
    return authToken !== null && authToken !== undefined;
}

// 获取当前用户信息
function getCurrentUser() {
    return currentUser;
}
