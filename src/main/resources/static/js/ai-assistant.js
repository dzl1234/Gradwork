/**
 * AI助手功能的JavaScript
 */

// 发送问题到AI
async function askAI(question, language, translateResponse = true, targetLanguage = null) {
    try {
        if (!targetLanguage && getCurrentUser()) {
            targetLanguage = getCurrentUser().preferredLanguage;
        }
        const authToken = localStorage.getItem('authToken');
        const response = await fetch('/api/ai/ask', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                question,
                language,
                translateResponse,
                targetLanguage
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'AI服务请求失败');
        }

        let data;
        try {
            data = await response.json();
        } catch (error) {
            console.error('JSON解析失败:', await response.text());
            throw new Error('AI响应格式错误');
        }
        return data;
    } catch (error) {
        console.error('AI请求错误:', error);
        return { success: false, message: error.message };
    }
}

// 显示AI回答
function showAIResponse(response) {
    const aiResponseContainer = document.getElementById('aiResponseContainer');
    if (!aiResponseContainer) return;

    // 清除加载状态
    aiResponseContainer.classList.remove('loading');

    if (!response.success) {
        aiResponseContainer.innerHTML = `
            <div class="ai-error">
                <div class="error-title">错误</div>
                <div class="error-message">${response.message}</div>
            </div>
        `;
        return;
    }

    // 显示AI回答
    aiResponseContainer.innerHTML = `
        <div class="ai-answer">
            <div class="answer-text">${response.answer}</div>
            ${response.originalAnswer ? `
                <div class="original-answer-toggle">查看原文</div>
                <div class="original-answer hidden">${response.originalAnswer}</div>
            ` : ''}
        </div>
    `;

    // 原文切换
    const originalToggle = aiResponseContainer.querySelector('.original-answer-toggle');
    const originalAnswer = aiResponseContainer.querySelector('.original-answer');

    if (originalToggle && originalAnswer) {
        originalToggle.addEventListener('click', function() {
            if (originalAnswer.classList.contains('hidden')) {
                originalAnswer.classList.remove('hidden');
                originalToggle.textContent = '隐藏原文';
            } else {
                originalAnswer.classList.add('hidden');
                originalToggle.textContent = '查看原文';
            }
        });
    }
}

// 提交AI问题
async function submitAIQuestion() {
    const aiQuestionInput = document.getElementById('aiQuestionInput');
    if (!aiQuestionInput) return;

    const question = aiQuestionInput.value.trim();
    if (question === '') {
        showNotification('提示', '请输入问题', 'info');
        return;
    }

    const aiResponseContainer = document.getElementById('aiResponseContainer');
    if (aiResponseContainer) {
        aiResponseContainer.innerHTML = '';
        aiResponseContainer.classList.add('loading');
    }

    // 获取语言设置
    let language = 'zh';  // 默认中文
    const languageSelect = document.getElementById('aiLanguageSelect');
    if (languageSelect) {
        language = languageSelect.value;
    }

    // 获取是否需要翻译
    let translateResponse = true;
    const translateCheck = document.getElementById('translateResponseCheck');
    if (translateCheck) {
        translateResponse = translateCheck.checked;
    }

    // 获取目标语言
    let targetLanguage = null;
    const targetLanguageSelect = document.getElementById('targetLanguageSelect');
    if (targetLanguageSelect && targetLanguageSelect.style.display !== 'none') {
        targetLanguage = targetLanguageSelect.value;
    }

    const response = await askAI(question, language, translateResponse, targetLanguage);
    showAIResponse(response);
}

// 切换翻译设置的显示
function toggleTranslationOptions() {
    const translateCheck = document.getElementById('translateResponseCheck');
    const targetLanguageContainer = document.getElementById('targetLanguageContainer');

    if (!translateCheck || !targetLanguageContainer) return;

    if (translateCheck.checked) {
        targetLanguageContainer.style.display = 'block';
    } else {
        targetLanguageContainer.style.display = 'none';
    }
}

// 显示/隐藏AI助手面板
function toggleAIAssistant() {
    const aiPanel = document.getElementById('aiAssistantPanel');
    if (!aiPanel) return;

    if (aiPanel.classList.contains('visible')) {
        aiPanel.classList.remove('visible');
    } else {
        aiPanel.classList.add('visible');
    }
}

// 初始化AI助手功能
document.addEventListener('DOMContentLoaded', function() {
    // AI助手面板切换按钮
    const toggleAIBtn = document.getElementById('toggleAIBtn');
    if (toggleAIBtn) {
        toggleAIBtn.addEventListener('click', toggleAIAssistant);
    }

    // 提交问题按钮
    const submitAIBtn = document.getElementById('submitAIBtn');
    if (submitAIBtn) {
        submitAIBtn.addEventListener('click', submitAIQuestion);
    }

    // 问题输入框回车提交
    const aiQuestionInput = document.getElementById('aiQuestionInput');
    if (aiQuestionInput) {
        aiQuestionInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                submitAIQuestion();
            }
        });
    }

    // 翻译选项切换
    const translateCheck = document.getElementById('translateResponseCheck');
    if (translateCheck) {
        translateCheck.addEventListener('change', toggleTranslationOptions);
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
