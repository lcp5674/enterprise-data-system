/**
 * 预置图表模板
 */
import React from 'react';
import Chart from '../Chart';
import type { EChartsOption } from 'echarts';

// 折线图配置
export const getLineChartOption = (
  title: string,
  xData: string[],
  seriesData: { name: string; data: number[] }[],
): EChartsOption => ({
  title: { text: title, left: 'center', textStyle: { fontSize: 16 } },
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: seriesData.map((s) => s.name) },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: xData },
  yAxis: { type: 'value' },
  series: seriesData.map((s) => ({
    name: s.name,
    type: 'line',
    data: s.data,
    smooth: true,
    areaStyle: {},
  })),
});

// 柱状图配置
export const getBarChartOption = (
  title: string,
  xData: string[],
  seriesData: { name: string; data: number[] }[],
): EChartsOption => ({
  title: { text: title, left: 'center', textStyle: { fontSize: 16 } },
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: seriesData.map((s) => s.name) },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: xData },
  yAxis: { type: 'value' },
  series: seriesData.map((s) => ({
    name: s.name,
    type: 'bar',
    data: s.data,
  })),
});

// 饼图配置
export const getPieChartOption = (
  title: string,
  data: { name: string; value: number }[],
): EChartsOption => ({
  title: { text: title, left: 'center', textStyle: { fontSize: 16 } },
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, orient: 'horizontal' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
      },
      data: data.map((item) => ({ name: item.name, value: item.value })),
    },
  ],
});

// 仪表盘配置
export const getGaugeChartOption = (
  title: string,
  value: number,
  min = 0,
  max = 100,
): EChartsOption => ({
  title: { text: title, left: 'center', textStyle: { fontSize: 16 } },
  series: [
    {
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min,
      max,
      splitNumber: 5,
      itemStyle: {
        color: value >= 80 ? '#52c41a' : value >= 60 ? '#faad14' : '#ff4d4f',
      },
      progress: { show: true, width: 18 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 18 } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      title: { show: false },
      detail: {
        valueAnimation: true,
        fontSize: 28,
        offsetCenter: [0, '10%'],
        formatter: '{value}%',
        color: '#333',
      },
      data: [{ value }],
    },
  ],
});

// 雷达图配置
export const getRadarChartOption = (
  title: string,
  indicator: { name: string; max: number }[],
  seriesData: { name: string; value: number[] }[],
): EChartsOption => ({
  title: { text: title, left: 'center', textStyle: { fontSize: 16 } },
  tooltip: {},
  legend: { bottom: 0, data: seriesData.map((s) => s.name) },
  radar: {
    indicator,
    radius: '60%',
  },
  series: seriesData.map((s) => ({
    name: s.name,
    type: 'radar',
    data: [{ name: s.name, value: s.value }],
  })),
});

// 质量评分趋势图
export const getQualityTrendOption = (
  xData: string[],
  scores: number[],
): EChartsOption => ({
  title: { text: '质量评分趋势', left: 'center', textStyle: { fontSize: 16 } },
  tooltip: { trigger: 'axis', formatter: '{b}: {c}分' },
  grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
  xAxis: { type: 'category', data: xData, boundaryGap: false },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100,
    splitLine: { lineStyle: { type: 'dashed' } },
  },
  series: [
    {
      type: 'line',
      data: scores,
      smooth: true,
      lineStyle: { width: 3, color: '#1890ff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
          ],
        },
      },
      markLine: {
        silent: true,
        data: [
          { yAxis: 60, lineStyle: { color: '#ff4d4f' }, name: '及格线' },
          { yAxis: 80, lineStyle: { color: '#52c41a' }, name: '优秀线' },
        ],
      },
    },
  ],
});

// 资产分布图
export const getAssetDistributionOption = (
  byType: { name: string; value: number }[],
  bySensitivity: { name: string; value: number }[],
): EChartsOption => ({
  title: [{ text: '资产类型分布', left: '25%', top: '5%', textAlign: 'center' }, {
    text: '敏感度分布',
    left: '75%',
    top: '5%',
    textAlign: 'center',
  }],
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: [{ bottom: 0, left: '10%' }, { bottom: 0, left: '60%' }],
  series: [
    {
      type: 'pie',
      radius: '40%',
      center: ['25%', '50%'],
      data: byType.map((item) => ({ name: item.name, value: item.value })),
    },
    {
      type: 'pie',
      radius: '40%',
      center: ['75%', '50%'],
      data: bySensitivity.map((item) => ({ name: item.name, value: item.value })),
    },
  ],
});

// 数据血缘依赖图
export const getLineageDependencyOption = (
  nodes: { id: string; name: string; type: string }[],
  links: { source: string; target: string }[],
): EChartsOption => ({
  title: { text: '血缘依赖关系', left: 'center', textStyle: { fontSize: 16 } },
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'sankey',
      layout: 'none',
      emphasis: { focus: 'adjacency' },
      nodeAlign: 'left',
      lineStyle: { curveness: 0.5 },
      data: nodes.map((n) => ({ name: n.name })),
      links: links.map((l) => ({
        source: nodes.find((n) => n.id === l.source)?.name || '',
        target: nodes.find((n) => n.id === l.target)?.name || '',
      })),
    },
  ],
});

// 预置图表组件
interface PresetChartProps {
  loading?: boolean;
  height?: number;
}

// 资产增长趋势图
export const AssetGrowthChart: React.FC<PresetChartProps> = ({ loading, height = 300 }) => {
  const option = getLineChartOption('资产增长趋势', ['1月', '2月', '3月', '4月', '5月', '6月'], [
    { name: '表', data: [120, 145, 178, 210, 256, 312] },
    { name: 'API', data: [80, 95, 120, 145, 178, 220] },
    { name: '文件', data: [50, 65, 80, 95, 110, 135] },
  ]);
  return <Chart option={option} height={height} loading={loading} />;
};

// 质量评分趋势图
export const QualityTrendChart: React.FC<PresetChartProps> = ({ loading, height = 300 }) => {
  const option = getQualityTrendOption(
    ['1月', '2月', '3月', '4月', '5月', '6月'],
    [75, 78, 72, 80, 82, 85],
  );
  return <Chart option={option} height={height} loading={loading} />;
};

// 资产分布图
export const AssetDistributionChart: React.FC<PresetChartProps> = ({
  loading,
  height = 300,
}) => {
  const option = getAssetDistributionOption(
    [
      { name: '数据库表', value: 35 },
      { name: 'API服务', value: 25 },
      { name: '数据文件', value: 20 },
      { name: '数据流', value: 15 },
      { name: '其他', value: 5 },
    ],
    [
      { name: '内部', value: 50 },
      { name: '敏感', value: 30 },
      { name: '高度敏感', value: 15 },
      { name: '公开', value: 5 },
    ],
  );
  return <Chart option={option} height={height} loading={loading} />;
};

// 质量评分仪表盘
export const QualityGaugeChart: React.FC<PresetChartProps & { score: number }> = ({
  loading,
  score = 0,
  height = 200,
}) => {
  const option = getGaugeChartOption('综合质量评分', score);
  return <Chart option={option} height={height} loading={loading} />;
};

// 问题趋势图
export const IssueTrendChart: React.FC<PresetChartProps> = ({ loading, height = 300 }) => {
  const option = getBarChartOption('质量问题趋势', ['1月', '2月', '3月', '4月', '5月', '6月'], [
    { name: '发现问题', data: [45, 52, 38, 65, 42, 35] },
    { name: '已修复', data: [40, 48, 35, 58, 40, 32] },
  ]);
  return <Chart option={option} height={height} loading={loading} />;
};

// 数据源分布饼图
export const DatasourcePieChart: React.FC<PresetChartProps> = ({ loading, height = 300 }) => {
  const option = getPieChartOption('数据源分布', [
    { name: 'MySQL', value: 35 },
    { name: 'PostgreSQL', value: 25 },
    { name: 'MongoDB', value: 15 },
    { name: 'Hive', value: 12 },
    { name: 'Kafka', value: 8 },
    { name: '其他', value: 5 },
  ]);
  return <Chart option={option} height={height} loading={loading} />;
};

export default Chart;
