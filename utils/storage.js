/**
 * 本地存储工具模块
 */

// 用户数据
function getUserData() {
  return wx.getStorageSync('userData') || {};
}

function setUserData(data) {
  wx.setStorageSync('userData', data);
}

// 聊天记录
function getChatHistory() {
  return wx.getStorageSync('chatHistory') || [];
}

function addChatMessage(role, content) {
  const history = getChatHistory();
  history.push({
    role,
    content,
    timestamp: Date.now()
  });
  wx.setStorageSync('chatHistory', history);
  return history;
}

function clearChatHistory() {
  wx.setStorageSync('chatHistory', []);
}

// 体重记录
function getWeightRecords() {
  return wx.getStorageSync('weightRecords') || [];
}

function addWeightRecord(record) {
  const records = getWeightRecords();
  const newRecord = {
    id: Date.now(),
    ...record,
    createdAt: new Date().toISOString()
  };
  records.unshift(newRecord);
  wx.setStorageSync('weightRecords', records);
  return records;
}

function updateWeightRecord(id, record) {
  const records = getWeightRecords();
  const index = records.findIndex(r => r.id === id);
  if (index !== -1) {
    records[index] = { ...records[index], ...record };
    wx.setStorageSync('weightRecords', records);
  }
  return records;
}

function deleteWeightRecord(id) {
  const records = getWeightRecords();
  const filtered = records.filter(r => r.id !== id);
  wx.setStorageSync('weightRecords', filtered);
  return filtered;
}

// 身体维度记录
function getMeasurementRecords() {
  return wx.getStorageSync('measurementRecords') || [];
}

function addMeasurementRecord(record) {
  const records = getMeasurementRecords();
  const newRecord = {
    id: Date.now(),
    ...record,
    createdAt: new Date().toISOString()
  };
  records.unshift(newRecord);
  wx.setStorageSync('measurementRecords', records);
  return records;
}

function deleteMeasurementRecord(id) {
  const records = getMeasurementRecords();
  const filtered = records.filter(r => r.id !== id);
  wx.setStorageSync('measurementRecords', filtered);
  return filtered;
}

// 经期记录
function getMenstrualRecords() {
  return wx.getStorageSync('menstrualRecords') || [];
}

function addMenstrualRecord(record) {
  const records = getMenstrualRecords();
  const newRecord = {
    id: Date.now(),
    ...record,
    createdAt: new Date().toISOString()
  };
  records.unshift(newRecord);
  wx.setStorageSync('menstrualRecords', records);
  return records;
}

module.exports = {
  getUserData,
  setUserData,
  getChatHistory,
  addChatMessage,
  clearChatHistory,
  getWeightRecords,
  addWeightRecord,
  updateWeightRecord,
  deleteWeightRecord,
  getMeasurementRecords,
  addMeasurementRecord,
  deleteMeasurementRecord,
  getMenstrualRecords,
  addMenstrualRecord
}