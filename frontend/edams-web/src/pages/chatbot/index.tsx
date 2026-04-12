/**
 * AI智能助手页面
 */

import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  Card,
  Input,
  Button,
  Space,
  List,
  Avatar,
  Collapse,
  Badge,
  Typography,
  Spin,
  Empty,
  Divider,
  message,
} from 'antd';
import {
  SendOutlined,
  MessageOutlined,
  PlusOutlined,
  DeleteOutlined,
  RobotOutlined,
  UserOutlined,
  HistoryOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import styles from './index.less';
import { chatbot } from '../../services';

const { TextArea } = Input;
const { Panel } = Collapse;

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  isLoading?: boolean;
}

interface Conversation {
  id: string;
  title: string;
  lastMessage: string;
  lastTime: string;
  messages: Message[];
}

const Chatbot: React.FC = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [currentSessionId, setCurrentSessionId] = useState<string>('');
  const [inputMessage, setInputMessage] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [historyCollapsed, setHistoryCollapsed] = useState(false);
  const [loadingConversations, setLoadingConversations] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 加载会话列表
  const loadConversations = useCallback(async () => {
    setLoadingConversations(true);
    try {
      const response = await chatbot.getConversations({ page: 1, size: 20 });
      if (response.success && response.data) {
        const sessions = response.data.records || response.data;
        // 转换为页面格式
        const mappedConversations: Conversation[] = sessions.map((session: any) => ({
          id: String(session.id),
          title: session.title || session.sessionTitle || '新会话',
          lastMessage: session.lastMessage || '暂无消息',
          lastTime: session.updatedTime || session.createdTime || new Date().toISOString(),
          messages: [],
        }));
        
        if (mappedConversations.length > 0) {
          setConversations(mappedConversations);
          setCurrentSessionId(mappedConversations[0].id);
          // 加载第一条会话的消息
          loadMessages(mappedConversations[0].id);
        } else {
          // 没有会话时创建新会话
          handleNewConversation();
        }
      }
    } catch (error) {
      console.error('加载会话列表失败:', error);
      // 失败时创建默认会话
      handleNewConversation();
    } finally {
      setLoadingConversations(false);
    }
  }, []);

  // 加载会话消息
  const loadMessages = async (sessionId: string) => {
    try {
      const response = await chatbot.getHistory(sessionId, { limit: 50 });
      if (response.success && response.data) {
        const messages = (response.data.records || response.data) as any[];
        
        setConversations((prev) =>
          prev.map((c) =>
            c.id === sessionId
              ? {
                  ...c,
                  messages: messages.map((m: any) => ({
                    id: String(m.id),
                    role: m.role === 'user_message' || m.role === 'user' ? 'user' : 'assistant',
                    content: m.content || m.message || '',
                    timestamp: m.createdTime || new Date().toISOString(),
                  })),
                }
              : c
          )
        );
      }
    } catch (error) {
      console.error('加载消息失败:', error);
    }
  };

  // 初始加载
  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  // 滚动到最新消息
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [conversations, currentSessionId]);

  // 获取当前会话
  const currentConversation = conversations.find((c) => c.id === currentSessionId);

  // 创建新会话
  const handleNewConversation = async () => {
    try {
      const response = await chatbot.createConversation({ title: '新会话' });
      if (response.success && response.data) {
        const newSession = {
          id: String(response.data.id || response.data.sessionId),
          title: response.data.title || '新会话',
          lastMessage: '你好！我是AI智能助手，有什么可以帮助您的吗？',
          lastTime: new Date().toISOString(),
          messages: [
            {
              id: `msg-${Date.now()}`,
              role: 'assistant' as const,
              content: '你好！我是AI智能助手，有什么可以帮助您的吗？\n\n我可以帮您：\n- 查询数据资产信息\n- 解答数据管理问题\n- 生成数据报表\n- 分析数据质量问题',
              timestamp: new Date().toISOString(),
            },
          ],
        };
        setConversations((prev) => [newSession, ...prev]);
        setCurrentSessionId(newSession.id);
      }
    } catch (error) {
      // API失败时使用本地会话
      const fallbackSession: Conversation = {
        id: `local-${Date.now()}`,
        title: `新会话 ${conversations.length + 1}`,
        lastMessage: '你好！我是AI智能助手，有什么可以帮助您的吗？',
        lastTime: new Date().toISOString(),
        messages: [
          {
            id: `msg-${Date.now()}`,
            role: 'assistant',
            content: '你好！我是AI智能助手，有什么可以帮助您的吗？\n\n我可以帮您：\n- 查询数据资产信息\n- 解答数据管理问题\n- 生成数据报表\n- 分析数据质量问题',
            timestamp: new Date().toISOString(),
          },
        ],
      };
      setConversations((prev) => [fallbackSession, ...prev]);
      setCurrentSessionId(fallbackSession.id);
    }
  };

  // 删除会话
  const handleDeleteConversation = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await chatbot.deleteConversation(id);
    } catch (error) {
      console.error('删除会话API失败:', error);
    }
    
    const newConversations = conversations.filter((c) => c.id !== id);
    if (newConversations.length === 0) {
      handleNewConversation();
    } else {
      setConversations(newConversations);
      if (currentSessionId === id) {
        setCurrentSessionId(newConversations[0].id);
      }
    }
    message.success('会话已删除');
  };

  // 发送消息
  const handleSend = async () => {
    if (!inputMessage.trim()) return;

    // 如果没有会话，先创建
    let sessionId = currentSessionId;
    if (!sessionId) {
      await handleNewConversation();
      sessionId = conversations[0]?.id;
      if (!sessionId) return;
    }

    const userMessage: Message = {
      id: `local-msg-${Date.now()}`,
      role: 'user',
      content: inputMessage.trim(),
      timestamp: new Date().toISOString(),
    };

    const loadingMessage: Message = {
      id: `local-msg-${Date.now() + 1}`,
      role: 'assistant',
      content: '正在分析...',
      timestamp: new Date().toISOString(),
      isLoading: true,
    };

    // 更新当前会话
    setConversations((prev) =>
      prev.map((c) =>
        c.id === sessionId
          ? {
              ...c,
              messages: [...c.messages, userMessage, loadingMessage],
              lastMessage: inputMessage.trim(),
              lastTime: new Date().toISOString(),
            }
          : c
      )
    );

    const tempInput = inputMessage;
    setInputMessage('');
    setIsSending(true);

    try {
      // 调用后端API
      const response = await chatbot.sendMessage({
        sessionId: sessionId,
        message: tempInput,
        context: {},
      });
      
      // 提取AI回复
      let aiResponse = '抱歉，我现在无法回答您的问题，请稍后再试。';
      if (response.success && response.data) {
        aiResponse = response.data.content || response.data.message || aiResponse;
      }

      const aiMessage: Message = {
        id: loadingMessage.id,
        role: 'assistant',
        content: aiResponse,
        timestamp: new Date().toISOString(),
      };

      setConversations((prev) =>
        prev.map((c) =>
          c.id === sessionId
            ? {
                ...c,
                messages: c.messages.map((m) => (m.id === loadingMessage.id ? aiMessage : m)),
                lastMessage: aiResponse.substring(0, 50) + (aiResponse.length > 50 ? '...' : ''),
              }
            : c
        )
      );
    } catch (error) {
      console.error('发送消息失败:', error);
      
      // API失败时使用本地模拟回复
      const aiResponses: Record<string, string> = {
        默认: `我已收到您的问题："${tempInput}"\n\n这是一个示例回复，展示AI助手可以如何响应您的查询。在实际应用中，这里将集成真实的AI服务来提供智能回答。\n\n您可以问我关于：\n- 数据资产的详细信息\n- 数据质量管理建议\n- 数据治理最佳实践\n- 系统使用指南`,
        你好: '你好！很高兴为您服务。请问有什么可以帮助您的？',
        帮助: '我可以帮您：\n\n1. **数据查询** - 查询数据资产、表、字段信息\n2. **问题诊断** - 分析数据质量问题\n3. **报表生成** - 生成各类数据报表\n4. **操作指导** - 解答系统使用问题\n\n请直接输入您的问题！',
        数据: '关于数据管理，我可以帮您：\n\n- 查询数据资产目录\n- 分析数据质量规则\n- 生成数据血缘图谱\n- 检查元数据一致性\n\n请问具体想了解哪方面？',
      };

      let responseContent = aiResponses['默认'];
      for (const [keyword, response] of Object.entries(aiResponses)) {
        if (keyword !== '默认' && tempInput.includes(keyword)) {
          responseContent = response;
          break;
        }
      }

      const aiMessage: Message = {
        id: loadingMessage.id,
        role: 'assistant',
        content: responseContent,
        timestamp: new Date().toISOString(),
      };

      setConversations((prev) =>
        prev.map((c) =>
          c.id === sessionId
            ? {
                ...c,
                messages: c.messages.map((m) => (m.id === loadingMessage.id ? aiMessage : m)),
                lastMessage: responseContent.substring(0, 50) + '...',
              }
            : c
        )
      );
    } finally {
      setIsSending(false);
    }
  };

  // 按Enter发送
  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // 格式化时间
  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={styles.container}>
      <Card className={styles.mainCard}>
        {/* 顶部：历史会话列表 */}
        <div className={styles.historySection}>
          <Collapse
            activeKey={historyCollapsed ? [] : ['history']}
            onChange={() => setHistoryCollapsed(!historyCollapsed)}
            ghost
          >
            <Panel
              header={
                <div className={styles.historyHeader}>
                  <HistoryOutlined />
                  <span>历史会话</span>
                  <Badge count={conversations.length} showZero />
                </div>
              }
              key="history"
            >
              <div className={styles.conversationList}>
                <Space style={{ marginBottom: 8 }}>
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleNewConversation}
                    className={styles.newChatBtn}
                  >
                    新建会话
                  </Button>
                  <Button
                    icon={<ReloadOutlined />}
                    onClick={loadConversations}
                    loading={loadingConversations}
                  >
                    刷新
                  </Button>
                </Space>
                <List
                  dataSource={conversations}
                  renderItem={(item) => (
                    <List.Item
                      className={`${styles.conversationItem} ${
                        item.id === currentSessionId ? styles.active : ''
                      }`}
                      onClick={() => setCurrentSessionId(item.id)}
                      actions={[
                        <Button
                          type="text"
                          size="small"
                          danger
                          icon={<DeleteOutlined />}
                          onClick={(e) => handleDeleteConversation(item.id, e)}
                        />,
                      ]}
                    >
                      <List.Item.Meta
                        avatar={<Avatar icon={<MessageOutlined />} />}
                        title={item.title}
                        description={
                          <div className={styles.conversationDesc}>
                            <span className={styles.lastMessage}>{item.lastMessage}</span>
                            <span className={styles.lastTime}>
                              {new Date(item.lastTime).toLocaleDateString('zh-CN')}
                            </span>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              </div>
            </Panel>
          </Collapse>
        </div>

        <Divider className={styles.divider} />

        {/* 中间：聊天区域 */}
        <div className={styles.chatSection}>
          <div className={styles.messagesContainer}>
            {currentConversation?.messages.length === 0 ? (
              <Empty description="开始新的对话吧" />
            ) : (
              <div className={styles.messagesList}>
                {currentConversation?.messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`${styles.messageWrapper} ${
                      msg.role === 'user' ? styles.userMessage : styles.assistantMessage
                    }`}
                  >
                    <div className={styles.messageContent}>
                      <Avatar
                        className={styles.messageAvatar}
                        icon={msg.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                        style={{
                          backgroundColor: msg.role === 'user' ? '#1890ff' : '#52c41a',
                        }}
                      />
                      <div
                        className={`${styles.messageBubble} ${
                          msg.role === 'user'
                            ? styles.userBubble
                            : styles.assistantBubble
                        }`}
                      >
                        {msg.isLoading ? (
                          <Space>
                            <Spin size="small" />
                            <span>{msg.content}</span>
                          </Space>
                        ) : (
                          <div className={styles.markdownContent}>
                            <ReactMarkdown>{msg.content}</ReactMarkdown>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className={styles.messageTime}>{formatTime(msg.timestamp)}</div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </div>
            )}
          </div>

          {/* 底部：输入区域 */}
          <div className={styles.inputSection}>
            <div className={styles.inputWrapper}>
              <TextArea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="请输入您的问题，按Enter发送，Shift+Enter换行..."
                autoSize={{ minRows: 2, maxRows: 4 }}
                disabled={isSending}
                className={styles.input}
              />
              <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={handleSend}
                loading={isSending}
                disabled={!inputMessage.trim()}
                className={styles.sendBtn}
              >
                发送
              </Button>
            </div>
            <div className={styles.inputHint}>
              <Space>
                <RobotOutlined />
                <span>AI助手由大模型驱动，回答仅供参考</span>
              </Space>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Chatbot;
