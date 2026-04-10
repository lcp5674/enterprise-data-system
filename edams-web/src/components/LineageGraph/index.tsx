/**
 * 数据血缘图组件 - 基于 G6
 */
import React, { useEffect, useRef, useCallback, useState } from 'react';
import { Card, Button, Space, Select, Input, Tooltip, Spin, message } from 'antd';
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  FullscreenOutlined,
  RedoOutlined,
  SyncOutlined,
  NodeExpandOutlined,
  NodeCollapseOutlined,
} from '@ant-design/icons';
import G6 from '@antv/g6';
import './LineageGraph.less';

export interface LineageNode {
  id: string;
  name: string;
  type: 'TABLE' | 'API' | 'FILE' | 'STREAM' | 'FIELD';
  tableType?: 'SOURCE' | 'STAGING' | 'WAREHOUSE' | 'DATAMART';
  database?: string;
  schema?: string;
  fields?: { name: string; type: string }[];
  qualityScore?: number;
  owner?: string;
  [key: string]: unknown;
}

export interface LineageEdge {
  id: string;
  source: string;
  target: string;
  type: 'TRANSFORM' | 'DERIVE' | 'REFERENCE' | 'LOAD';
  transformation?: string;
  schedule?: string;
  [key: string]: unknown;
}

export interface LineageGraphProps {
  /** 节点数据 */
  nodes: LineageNode[];
  /** 边数据 */
  edges: LineageEdge[];
  /** 高度 */
  height?: number;
  /** 加载状态 */
  loading?: boolean;
  /** 是否显示上游 */
  showUpstream?: boolean;
  /** 是否显示下游 */
  showDownstream?: boolean;
  /** 展开层级 */
  expandLevel?: number;
  /** 点击节点回调 */
  onNodeClick?: (node: LineageNode) => void;
  /** 点击边回调 */
  onEdgeClick?: (edge: LineageEdge) => void;
  /** 展开节点回调 */
  onExpandNode?: (node: LineageNode) => void;
  /** 视图切换回调 */
  onViewChange?: (direction: 'upstream' | 'downstream' | 'both') => void;
  /** 导出图片 */
  onExportImage?: () => void;
}

// 注册自定义节点
const registerNodes = () => {
  // 表节点
  G6.registerNode('table-node', {
    draw(cfg: any, group: any) {
      const { type, tableType, qualityScore } = cfg;
      
      // 背景颜色
      const bgColors: Record<string, string> = {
        SOURCE: '#e6f7ff',
        STAGING: '#fff7e6',
        WAREHOUSE: '#f6ffed',
        DATAMART: '#fff0f0',
      };
      
      const width = 180;
      const height = 60;
      const color = '#1890ff';
      
      // 主体
      group.addShape('rect', {
        attrs: {
          x: 0,
          y: 0,
          width,
          height,
          fill: bgColors[tableType] || '#f5f5f5',
          stroke: color,
          lineWidth: 2,
          radius: 4,
          shadowColor: 'rgba(0,0,0,0.1)',
          shadowBlur: 4,
          shadowOffsetX: 2,
          shadowOffsetY: 2,
        },
        name: 'main-rect',
      });
      
      // 类型标签背景
      group.addShape('rect', {
        attrs: {
          x: 4,
          y: 4,
          width: 50,
          height: 18,
          fill: color,
          radius: 2,
        },
        name: 'type-bg',
      });
      
      // 类型标签文字
      group.addShape('text', {
        attrs: {
          x: 29,
          y: 15,
          text: tableType || 'TABLE',
          fontSize: 10,
          fill: '#fff',
          textAlign: 'center',
          textBaseline: 'middle',
        },
        name: 'type-text',
      });
      
      // 节点名称
      group.addShape('text', {
        attrs: {
          x: width / 2,
          y: 38,
          text: cfg.name || '',
          fontSize: 12,
          fill: '#333',
          textAlign: 'center',
          textBaseline: 'middle',
          fontWeight: 'bold',
        },
        name: 'name-text',
      });
      
      // 质量评分（如果有）
      if (qualityScore !== undefined) {
        group.addShape('text', {
          attrs: {
            x: width - 20,
            y: 15,
            text: `${qualityScore}%`,
            fontSize: 10,
            fill: qualityScore >= 80 ? '#52c41a' : qualityScore >= 60 ? '#faad14' : '#ff4d4f',
            textAlign: 'center',
          },
          name: 'quality-text',
        });
      }
      
      // 锚点
      group.addShape('circle', {
        attrs: {
          x: 0,
          y: height / 2,
          r: 4,
          fill: color,
          stroke: '#fff',
          lineWidth: 2,
        },
        name: 'left-anchor',
      });
      
      group.addShape('circle', {
        attrs: {
          x: width,
          y: height / 2,
          r: 4,
          fill: color,
          stroke: '#fff',
          lineWidth: 2,
        },
        name: 'right-anchor',
      });
      
      return group;
    },
    setState(name: string, value: string | boolean, item: any) {
      const group = item.getContainer();
      const mainRect = group.find((e: any) => e.get('name') === 'main-rect');
      
      if (name === 'hover') {
        mainRect.attr('shadowBlur', 8);
        mainRect.attr('shadowColor', 'rgba(24, 144, 255, 0.3)');
      } else if (name === 'selected') {
        mainRect.attr('stroke', '#1890ff');
        mainRect.attr('lineWidth', 3);
      } else if (name === 'highlight') {
        mainRect.attr('stroke', '#52c41a');
        mainRect.attr('shadowColor', 'rgba(82, 196, 26, 0.3)');
      }
    },
    getAnchorPoints() {
      return [
        [0, 0.5],  // 左侧
        [1, 0.5],  // 右侧
      ];
    },
  }, 'single-node');
  
  // API 节点
  G6.registerNode('api-node', {
    draw(cfg: any, group: any) {
      const width = 160;
      const height = 50;
      const color = '#722ed1';
      
      group.addShape('rect', {
        attrs: {
          x: 0,
          y: 0,
          width,
          height,
          fill: '#f9f0ff',
          stroke: color,
          lineWidth: 2,
          radius: 25,
        },
        name: 'main-rect',
      });
      
      group.addShape('text', {
        attrs: {
          x: width / 2,
          y: height / 2,
          text: cfg.name || '',
          fontSize: 12,
          fill: '#333',
          textAlign: 'center',
          textBaseline: 'middle',
        },
        name: 'name-text',
      });
      
      return group;
    },
    setState(name: string, value: string | boolean, item: any) {
      const group = item.getContainer();
      const mainRect = group.find((e: any) => e.get('name') === 'main-rect');
      
      if (name === 'hover') {
        mainRect.attr('shadowBlur', 6);
      }
    },
    getAnchorPoints() {
      return [[0, 0.5], [1, 0.5]];
    },
  }, 'single-node');
  
  // 文件节点
  G6.registerNode('file-node', {
    draw(cfg: any, group: any) {
      const width = 140;
      const height = 50;
      const color = '#52c41a';
      
      group.addShape('rect', {
        attrs: {
          x: 0,
          y: 0,
          width,
          height,
          fill: '#f6ffed',
          stroke: color,
          lineWidth: 2,
          radius: 4,
        },
        name: 'main-rect',
      });
      
      group.addShape('text', {
        attrs: {
          x: width / 2,
          y: height / 2,
          text: cfg.name || '',
          fontSize: 12,
          fill: '#333',
          textAlign: 'center',
          textBaseline: 'middle',
        },
        name: 'name-text',
      });
      
      return group;
    },
    setState(name: string, value: string | boolean, item: any) {
      const group = item.getContainer();
      const mainRect = group.find((e: any) => e.get('name') === 'main-rect');
      
      if (name === 'hover') {
        mainRect.attr('shadowBlur', 6);
      }
    },
    getAnchorPoints() {
      return [[0, 0.5], [1, 0.5]];
    },
  }, 'single-node');
};

// 注册边样式
const registerEdges = () => {
  G6.registerEdge('lineage-edge', {
    draw(cfg: any, group: any) {
      const { startPoint, endPoint } = cfg;
      const color = '#8c8c8c';
      
      const path = group.addShape('path', {
        attrs: {
          path: [
            ['M', startPoint.x, startPoint.y],
            ['L', endPoint.x, endPoint.y],
          ],
          stroke: color,
          lineWidth: 1.5,
          endArrow: true,
        },
        name: 'edge-path',
      });
      
      // 转换类型标签
      if (cfg.type) {
        const midX = (startPoint.x + endPoint.x) / 2;
        const midY = (startPoint.y + endPoint.y) / 2;
        
        group.addShape('rect', {
          attrs: {
            x: midX - 30,
            y: midY - 10,
            width: 60,
            height: 20,
            fill: '#fff',
            stroke: color,
            radius: 2,
          },
          name: 'label-bg',
        });
        
        group.addShape('text', {
          attrs: {
            x: midX,
            y: midY,
            text: cfg.type || '',
            fontSize: 10,
            fill: '#666',
            textAlign: 'center',
            textBaseline: 'middle',
          },
          name: 'label-text',
        });
      }
      
      return path;
    },
    setState(name: string, value: string | boolean, item: any) {
      const group = item.getContainer();
      const edge = group.find((e: any) => e.get('name') === 'edge-path');
      
      if (name === 'hover') {
        edge.attr('stroke', '#1890ff');
        edge.attr('lineWidth', 2.5);
      } else if (name === 'highlight') {
        edge.attr('stroke', '#52c41a');
        edge.attr('lineWidth', 3);
      }
    },
  }, 'single-edge');
};

registerNodes();
registerEdges();

const LineageGraph: React.FC<LineageGraphProps> = ({
  nodes,
  edges,
  height = 600,
  loading = false,
  showUpstream = true,
  showDownstream = true,
  expandLevel = 2,
  onNodeClick,
  onEdgeClick,
  onExpandNode,
  onViewChange,
  onExportImage,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const graphRef = useRef<G6.Graph | null>(null);
  const [zoom, setZoom] = useState(1);
  const [layout, setLayout] = useState<'LR' | 'TB' | 'RL' | 'BT'>('LR');
  const [isFullscreen, setIsFullscreen] = useState(false);
  
  // 初始化图表
  const initGraph = useCallback(() => {
    if (!containerRef.current || graphRef.current) return;
    
    const graph = new G6.Graph({
      container: containerRef.current,
      width: containerRef.current.offsetWidth,
      height,
      fitView: true,
      animate: true,
      defaultNode: { type: 'table-node' },
      defaultEdge: { type: 'lineage-edge' },
      modes: {
        default: ['drag-canvas', 'zoom-canvas', 'drag-node'],
      },
      layout: {
        type: 'dagre',
        rankdir: layout,
        nodesep: 60,
        ranksep: 100,
      },
    });
    
    graph.on('node:click', (evt: any) => {
      const { item } = evt;
      const model = item.getModel() as LineageNode;
      if (onNodeClick && model) onNodeClick(model);
    });
    
    graph.on('edge:click', (evt: any) => {
      const { item } = evt;
      const model = item.getModel() as LineageEdge;
      if (onEdgeClick && model) onEdgeClick(model);
    });
    
    graph.on('node:dblclick', (evt: any) => {
      const { item } = evt;
      const model = item.getModel() as LineageNode;
      if (onExpandNode && model) onExpandNode(model);
    });
    
    graph.on('wheel', () => {
      setZoom(graph.getZoom());
    });
    
    graphRef.current = graph;
  }, [height, layout, onNodeClick, onEdgeClick, onExpandNode]);
  
  // 渲染数据
  const renderData = useCallback(() => {
    if (!graphRef.current) return;
    
    const graph = graphRef.current;
    
    graph.changeData({
      nodes: nodes.map((node) => ({
        ...node,
        type: node.type === 'TABLE' ? 'table-node' : node.type === 'API' ? 'api-node' : 'file-node',
      })),
      edges: edges.map((edge, idx) => ({
        ...edge,
        id: `edge-${idx}`,
      })),
    });
    
    graph.fitView();
  }, [nodes, edges]);
  
  // 初始化和更新
  useEffect(() => {
    initGraph();
    return () => {
      if (graphRef.current) {
        graphRef.current.destroy();
        graphRef.current = null;
      }
    };
  }, [initGraph]);
  
  useEffect(() => {
    if (nodes.length > 0) {
      renderData();
    }
  }, [nodes, edges, renderData]);
  
  // 工具函数
  const handleZoomIn = () => {
    if (graphRef.current) {
      graphRef.current.zoom(1.2);
      setZoom(graphRef.current.getZoom());
    }
  };
  
  const handleZoomOut = () => {
    if (graphRef.current) {
      graphRef.current.zoom(0.8);
      setZoom(graphRef.current.getZoom());
    }
  };
  
  const handleFitView = () => {
    graphRef.current?.fitView();
  };
  
  const handleReset = () => {
    graphRef.current?.resetZoom();
    graphRef.current?.fitView();
    setZoom(1);
  };
  
  const handleAutoLayout = () => {
    if (graphRef.current) {
      const layouts: Array<'LR' | 'TB' | 'RL' | 'BT'> = ['LR', 'TB', 'RL', 'BT'];
      const currentIndex = layouts.indexOf(layout);
      const nextLayout = layouts[(currentIndex + 1) % layouts.length];
      setLayout(nextLayout);
      graphRef.current.changeLayout({ type: 'dagre', rankdir: nextLayout });
    }
  };
  
  const handleFullscreen = () => {
    if (!containerRef.current) return;
    
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };
  
  const handleExport = () => {
    if (graphRef.current) {
      const dataURL = graphRef.current.toDataURL('image/png', '#fff');
      const link = document.createElement('a');
      link.download = 'lineage-graph.png';
      link.href = dataURL;
      link.click();
      message.success('血缘图已导出');
      if (onExportImage) onExportImage();
    }
  };
  
  return (
    <Card className="lineage-graph-card" bodyStyle={{ padding: 0 }}>
      {/* 工具栏 */}
      <div className="lineage-toolbar">
        <Space>
          <Tooltip title="放大">
            <Button icon={<ZoomInOutlined />} onClick={handleZoomIn} />
          </Tooltip>
          <Tooltip title="缩小">
            <Button icon={<ZoomOutOutlined />} onClick={handleZoomOut} />
          </Tooltip>
          <span className="zoom-level">{Math.round(zoom * 100)}%</span>
          <Tooltip title="适应画布">
            <Button icon={<FullscreenOutlined />} onClick={handleFitView} />
          </Tooltip>
          <Tooltip title="重置">
            <Button icon={<RedoOutlined />} onClick={handleReset} />
          </Tooltip>
          <Tooltip title="自动布局">
            <Button icon={<SyncOutlined />} onClick={handleAutoLayout} />
          </Tooltip>
        </Space>
        
        <Space>
          <Select
            value={layout}
            onChange={(v) => {
              setLayout(v);
              if (graphRef.current) {
                graphRef.current.changeLayout({ type: 'dagre', rankdir: v });
              }
            }}
            options={[
              { value: 'LR', label: '从左到右' },
              { value: 'TB', label: '从上到下' },
              { value: 'RL', label: '从右到左' },
              { value: 'BT', label: '从下到上' },
            ]}
            style={{ width: 100 }}
          />
          
          <Button onClick={handleFullscreen}>
            <FullscreenOutlined /> {isFullscreen ? '退出全屏' : '全屏'}
          </Button>
          
          <Button type="primary" onClick={handleExport}>
            导出图片
          </Button>
        </Space>
      </div>
      
      {/* 图例 */}
      <div className="lineage-legend">
        <span className="legend-item">
          <span className="legend-color" style={{ background: '#e6f7ff' }} />
          <span>源数据</span>
        </span>
        <span className="legend-item">
          <span className="legend-color" style={{ background: '#fff7e6' }} />
          <span>临时数据</span>
        </span>
        <span className="legend-item">
          <span className="legend-color" style={{ background: '#f6ffed' }} />
          <span>数据仓库</span>
        </span>
        <span className="legend-item">
          <span className="legend-color" style={{ background: '#fff0f0' }} />
          <span>数据集市</span>
        </span>
      </div>
      
      {/* 图表容器 */}
      <div
        ref={containerRef}
        className="lineage-graph-container"
        style={{ height, position: 'relative' }}
      >
        {loading && (
          <div className="loading-mask">
            <Spin tip="加载血缘关系..." />
          </div>
        )}
      </div>
      
      {/* 提示 */}
      <div className="lineage-tips">
        <span>单击节点查看详情</span>
        <span>双击节点展开更多</span>
        <span>拖拽节点调整位置</span>
      </div>
    </Card>
  );
};

export default LineageGraph;
