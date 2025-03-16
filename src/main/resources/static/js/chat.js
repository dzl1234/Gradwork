/**
 * 聊天功能的JavaScript
 */

// WebSocket连接
let stompClient = null;
let selectedUser = null;
let chatMessages = {};

// 初始化WebSocket连接
function connectWebSocket() {
    // 断开之前的连接
    if (stompClient !== null) {
        stompClient.disconnect();
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    const headers = {
        'Authorization': localStorage.getItem('authToken')
    };

    stompClient.connect(headers, function() {
        // 订阅个人消息频道
        stompClient.subscribe('/user/queue/messages', onMessageReceived);

        // 订阅用户状态变更
        stompClient.subscribe('/topic/status', onStatusChange);

        // 向服务器发送上线状态
        sendStatusUpdate(true);

        // 加载好友列表
        loadFriendsList();
    }, onError);
}

// 发送状态更新
function sendStatusUpdate(online) {
    if (stompClient) {
        stompClient.send("/app/status", {}, JSON.stringify({
            userId: getCurrentUser().id,
            online: online
        }));
    }
}

// 加载好友列表
async function loadFriendsList() {
    try {
        const response = await fetch('http://localhost:8080/api/friends', {
            headers: {
                ...getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error('加载好友列表失败');
        }

        const friends = await response.json();
        displayFriends(friends);
    } catch (error) {
        console.error('加载好友列表错误:', error);
        showNotification('错误', '无法加载好友列表', 'error');
    }
}

// 显示好友列表
function displayFriends(friends) {
    const friendsList = document.getElementById('friendsList');
    friendsList.innerHTML = '';

    friends.forEach(friend => {
        const friendItem = document.createElement('div');
        friendItem.classList.add('friend-item');
        if (friend.online) {
            friendItem.classList.add('online');
        }

        friendItem.innerHTML = `
            <div class="friend-name">${friend.username}</div>
            <div class="friend-status">${friend.online ? '在线' : '离线'}</div>
            <div class="friend-language">${friend.preferredLanguage}</div>
        `;

        friendItem.addEventListener('click', () => selectUser(friend));
        friendsList.appendChild(friendItem);
    });
}

// 选择聊天用户
function selectUser(user) {
    selectedUser = user;
    document.querySelectorAll('.friend-item').forEach(item => item.classList.remove('selected'));

    // 高亮选中的用户
    document.querySelectorAll('.friend-item').forEach(item => {
        if (item.querySelector('.friend-name').textContent === user.username) {
            item.classList.add('selected');
        }
    });

    loadChatHistory(user.id);

    // 更新聊天框标题
    document.getElementById('chatTitle').textContent = `与 ${user.username} 聊天中`;
}

// 加载聊天历史
async function loadChatHistory(userId) {
    try {
        const response = await fetch(`http://localhost:8080/api/chat/history/${userId}`, {
            headers: {
                ...getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error('加载聊天历史失败');
        }

        const messages = await response.json();
        displayChatHistory(messages);
    } catch (error) {
        console.error('加载聊天历史错误:', error);
        showNotification('错误', '无法加载聊天历史', 'error');
    }
}

// 显示聊天历史
function displayChatHistory(messages) {
    const chatContainer = document.getElementById('chatMessages');
    chatContainer.innerHTML = '';

    messages.forEach(message => {
        addMessageToChat(message);
    });

    // 滚动到底部
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

// 添加消息到聊天框
function addMessageToChat(message) {
    const chatContainer = document.getElementById('chatMessages');
    const messageElement = document.createElement('div');

    const currentUser = getCurrentUser();
    const isOwnMessage = message.senderId === currentUser.id;

    messageElement.classList.add('message');
    messageElement.classList.add(isOwnMessage ? 'own-message' : 'other-message');

    // 消息内容，显示原始消息和翻译后的消息
    let messageContent = '';

    if (isOwnMessage) {
        messageContent = `
            <div class="message-content">
                <div class="original-message">${message.originalContent}</div>
                ${message.translatedContent ? `<div class="translated-message">(翻译: ${message.translatedContent})</div>` : ''}
            </div>
            <div class="message-time">${formatTimestamp(message.timestamp)}</div>
        `;
    } else {
        messageContent = `
            <div class="message-sender">${message.senderName}</div>
            <div class="message-content">
                ${message.translatedContent ? `<div class="translated-message">${message.translatedContent}</div>` : ''}
                <div class="original-message">(原文: ${message.originalContent})</div>
            </div>
            <div class="message-time">${formatTimestamp(message.timestamp)}</div>
        `;
    }

    messageElement.innerHTML = messageContent;
    chatContainer.appendChild(messageElement);

    // 滚动到底部
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

// 发送消息
function sendMessage() {
    if (!selectedUser) {
        showNotification('提示', '请先选择一位好友聊天', 'info');
        return;
    }

    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();

    if (content === '') {
        return;
    }

    const currentUser = getCurrentUser();

    const chatMessage = {
        senderId: currentUser.id,
        receiverId: selectedUser.id,
        originalContent: content,
        originalLanguage: currentUser.preferredLanguage,
        targetLanguage: selectedUser.preferredLanguage
    };

    fetch('http://localhost:8080/api/chat/send', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...getAuthHeader()
        },
        body: JSON.stringify(chatMessage)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('发送消息失败');
            }
            return response.json();
        })
        .then(data => {
            messageInput.value = '';
            loadChatHistory(selectedUser.id);
        })
        .catch(error => {
            console.error('发送消息错误:', error);
            showNotification('错误', error.message, 'error');
        });
}

// 接收消息
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    addMessageToChat(message);

    // 如果是对方发来的消息，显示通知
    const currentUser = getCurrentUser();
    if (message.senderId !== currentUser.id) {
        showNotification('新消息', `来自 ${message.senderName} 的新消息`, 'info');
    }
}

// 状态变更
function onStatusChange(payload) {
    const statusUpdate = JSON.parse(payload.body);

    // 更新好友列表中的状态
    document.querySelectorAll('.friend-item').forEach(item => {
        const friendName = item.querySelector('.friend-name').textContent;
        if (statusUpdate.username === friendName) {
            item.querySelector('.friend-status').textContent = statusUpdate.online ? '在线' : '离线';
            if (statusUpdate.online) {
                item.classList.add('online');
            } else {
                item.classList.remove('online');
            }
        }
    });
}

// 连接错误处理
function onError(error) {
    console.error('WebSocket连接错误:', error);
    showNotification('错误', '连接服务器失败，请刷新页面重试', 'error');
}

// 格式化时间戳
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString();
}

// 显示通知
function showNotification(title, message, type) {
    const notificationContainer = document.getElementById('notificationContainer');
    if (!notificationContainer) return;

    const notification = document.createElement('div');
    notification.classList.add('notification', type);

    notification.innerHTML = `
        <div class="notification-title">${title}</div>
        <div class="notification-message">${message}</div>
    `;

    notificationContainer.appendChild(notification);

    // 3秒后自动移除
    setTimeout(() => {
        notification.classList.add('fadeout');
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 3000);
}

// 添加好友功能
async function addFriend() {
    const addFriendInput = document.getElementById('addFriendInput');
    const username = addFriendInput.value.trim();

    if (username === '') {
        showNotification('提示', '请输入好友用户名', 'info');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/friends/add', {
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

        showNotification('成功', '好友添加成功', 'success');
        addFriendInput.value = '';

        // 重新加载好友列表
        loadFriendsList();
    } catch (error) {
        console.error('添加好友错误:', error);
        showNotification('错误', error.message, 'error');
    }
}

// 页面加载
document.addEventListener('DOMContentLoaded', function() {
    // 检查是否已登录
    // if (!isLoggedIn()) {
    //     window.location.href = '../html/login.html';
    //     return;
    // }

    // 建立WebSocket连接
    connectWebSocket();

    // 发送消息按钮
    const sendButton = document.getElementById('sendButton');
    if (sendButton) {
        sendButton.addEventListener('click', sendMessage);
    }

    // 输入框回车发送
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    // 添加好友按钮
    const addFriendButton = document.getElementById('addFriendButton');
    if (addFriendButton) {
        addFriendButton.addEventListener('click', addFriend);
    }

    // 页面关闭前断开WebSocket
    window.addEventListener('beforeunload', function() {
        if (stompClient) {
            // 发送离线状态
            sendStatusUpdate(false);
            stompClient.disconnect();
        }
    });
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
