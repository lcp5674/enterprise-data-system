/**
 * 图表可视化组件 - ECharts 封装
 */
import React, { useEffect, useRef, useCallback } from 'react';
import * as echarts from 'echarts/core';
import {
  LineChart,
  BarChart,
  PieChart,
  GaugeChart,
  ScatterChart,
  RadarChart,
  FunnelChart,
  SankeyChart,
} from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  VisualMapComponent,
  ToolboxComponent,
  TimelineComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsOption } from 'echarts';
import { Spin } from 'antd';

// 注册 ECharts 组件
echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GaugeChart,
  ScatterChart,
  RadarChart,
  FunnelChart,
  SankeyChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  VisualMapComponent,
  ToolboxComponent,
  TimelineComponent,
  CanvasRenderer,
]);

export interface ChartProps {
  /** 图表配置选项 */
  option: EChartsOption;
  /** 图表宽度 */
  width?: string | number;
  /** 图表高度 */
  height?: string | number;
  /** 是否显示加载状态 */
  loading?: boolean;
  /** 加载文案 */
  loadingText?: string;
  /** 是否自适应容器 */
  autoResize?: boolean;
  /** 图表类名 */
  className?: string;
  /** 样式 */
  style?: React.CSSProperties;
  /** 点击事件回调 */
  onClick?: (params: unknown) => void;
  /** 绑定其他 ECharts 实例方法 */
  onChartReady?: (chart: echarts.ECharts) => void;
}

const Chart: React.FC<ChartProps> = ({
  option,
  width = '100%',
  height = 400,
  loading = false,
  loadingText = '加载中...',
  autoResize = true,
  className,
  style,
  onClick,
  onChartReady,
}) => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstanceRef = useRef<echarts.ECharts | null>(null);

  // 初始化图表
  const initChart = useCallback(() => {
    if (!chartRef.current) return;

    // 如果已存在实例，先销毁
    if (chartInstanceRef.current) {
      chartInstanceRef.current.dispose();
    }

    // 创建新实例
    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    // 设置配置
    chart.setOption(option);

    // 绑定点击事件
    if (onClick) {
      chart.on('click', onClick);
    }

    // 回调
    if (onChartReady) {
      onChartReady(chart);
    }
  }, [option, onClick, onChartReady]);

  // 更新图表
  useEffect(() => {
    if (chartInstanceRef.current) {
      chartInstanceRef.current.setOption(option, true);
    }
  }, [option]);

  // 初始化和自动调整大小
  useEffect(() => {
    initChart();

    if (autoResize) {
      const resizeObserver = new ResizeObserver(() => {
        chartInstanceRef.current?.resize();
      });

      if (chartRef.current) {
        resizeObserver.observe(chartRef.current);
      }

      return () => {
        resizeObserver.disconnect();
        chartInstanceRef.current?.dispose();
      };
    }

    return () => {
      chartInstanceRef.current?.dispose();
    };
  }, [initChart, autoResize]);

  return (
    <div
      ref={chartRef}
      className={className}
      style={{
        width,
        height,
        ...style,
      }}
    >
      {loading && (
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
          }}
        >
          <Spin tip={loadingText} />
        </div>
      )}
    </div>
  );
};

export default Chart;
