const app = getApp();
const { API_BASE } = require('../../config/api');

Page({
  data: {
    inputText: '',
    messages: [],
    scrollTop: 0,
    sessionId: ''  // AI对话会话ID
  },

  onLoad: function() {
    // 加载聊天历史
    const history = wx.getStorageSync('chatHistory') || [];
    // 加载会话ID
    const sessionId = wx.getStorageSync('chatSessionId') || '';
    this.setData({ messages: history, sessionId: sessionId });
    // 如果没有sessionId，创建一个新会话
    if (!sessionId) {
      this.createSession();
    }
  },

  // 创建新会话
  createSession: function() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    wx.request({
      url: API_BASE + '/chat/session',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success && res.data.data) {
          this.setData({ sessionId: res.data.data });
          wx.setStorageSync('chatSessionId', res.data.data);
          console.log('创建新会话:', res.data.data);
        }
      }
    });
  },

  // 清除会话并重新开始
  clearSession: function() {
    wx.showModal({
      title: '确认清空',
      content: '确定要开始新的对话吗？当前对话历史将被清空',
      success: (res) => {
        if (res.confirm) {
          this.setData({ messages: [], sessionId: '' });
          wx.removeStorageSync('chatHistory');
          wx.removeStorageSync('chatSessionId');
          this.createSession();
        }
      }
    });
  },

  onInput: function(e) {
    this.setData({ inputText: e.detail.value });
  },

  sendMessage: function() {
    const text = this.data.inputText.trim();
    if (!text) return;

    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: text,
      time: this.formatTime(new Date())
    };

    const messages = [...this.data.messages, userMessage];
    this.setData({
      messages: messages,
      inputText: '',
      scrollTop: this.data.scrollTop + 100
    });

    // 保存聊天历史
    wx.setStorageSync('chatHistory', messages);

    // 调用 AI 接口
    wx.showLoading({ title: '思考中...' });

    const sessionId = this.data.sessionId;
    const header = {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    };
    // 添加会话ID（如果存在）
    if (sessionId) {
      header['X-Session-Id'] = sessionId;
    }

    wx.request({
      url: API_BASE + '/chat/send',
      method: 'POST',
      header: header,
      data: {
        message: text
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.success) {
          const botMessage = {
            id: Date.now() + 1,
            role: 'bot',
            content: res.data.data.reply,
            time: this.formatTime(new Date())
          };
          const updatedMessages = [...this.data.messages, botMessage];
          this.setData({
            messages: updatedMessages,
            scrollTop: this.data.scrollTop + 200
          });
          wx.setStorageSync('chatHistory', updatedMessages);
        } else {
          wx.showToast({ title: res.data.message || 'AI回复失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  formatTime: function(date) {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  },

  onScrollUpper: function() {
    // 可以实现加载更多历史消息
  }
});
