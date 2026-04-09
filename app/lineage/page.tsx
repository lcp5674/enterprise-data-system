"use client";

import { useState, useCallback } from "react";
import { MainLayout } from "@/components/layout/main-layout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { SecurityBadge } from "@/components/ui/security-badge";
import { QualityScore } from "@/components/ui/quality-score";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  GitBranch,
  Search,
  ZoomIn,
  ZoomOut,
  Maximize2,
  Download,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  ArrowRight,
  RotateCcw,
  Filter,
  Database,
  Table2,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
} from "lucide-react";
import Link from "next/link";
import { cn } from "@/lib/utils";
import type { DataAsset, LineageNode, LineageEdge } from "@/types";

// Mock lineage data
const mockLineageNodes: LineageNode[] = [
  { id: "1", guid: "erp-001", name: "erp_orders", description: "ERP订单源表", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 90, usage_count: 45, owner_id: "1", tags: ["erp"], created_at: "", updated_at: "" },
  { id: "2", guid: "erp-002", name: "erp_customer", description: "ERP客户源表", type: "table", source_id: "1", schema_definition: null, security_level: "sensitive", quality_score: 88, usage_count: 32, owner_id: "2", tags: ["erp"], created_at: "", updated_at: "" },
  { id: "3", guid: "crm-001", name: "crm_customer_ext", description: "CRM客户扩展信息", type: "table", source_id: "1", schema_definition: null, security_level: "sensitive", quality_score: 85, usage_count: 28, owner_id: "2", tags: ["crm"], created_at: "", updated_at: "" },
  { id: "4", guid: "ods-001", name: "ods_orders", description: "订单原始数据表", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 92, usage_count: 156, owner_id: "1", tags: ["ods"], created_at: "", updated_at: "" },
  { id: "5", guid: "ods-002", name: "ods_customer", description: "客户原始数据表", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 89, usage_count: 134, owner_id: "2", tags: ["ods"], created_at: "", updated_at: "" },
  { id: "6", guid: "dwd-001", name: "dwd_orders_detail", description: "订单明细宽表", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 91, usage_count: 189, owner_id: "1", tags: ["dwd"], created_at: "", updated_at: "" },
  { id: "7", guid: "dwd-002", name: "dwd_customer_profile", description: "客户画像宽表", type: "table", source_id: "1", schema_definition: null, security_level: "sensitive", quality_score: 87, usage_count: 145, owner_id: "2", tags: ["dwd"], created_at: "", updated_at: "" },
  { id: "8", guid: "dws-001", name: "dws_order_stats_daily", description: "每日订单统计", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 94, usage_count: 98, owner_id: "3", tags: ["dws"], created_at: "", updated_at: "" },
  { id: "9", guid: "ads-001", name: "ads_revenue_report", description: "收入报表", type: "metric", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 95, usage_count: 78, owner_id: "3", tags: ["ads"], created_at: "", updated_at: "" },
  { id: "10", guid: "ads-002", name: "ads_customer_analysis", description: "客户分析报表", type: "metric", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 93, usage_count: 65, owner_id: "3", tags: ["ads"], created_at: "", updated_at: "" },
  { id: "11", guid: "api-001", name: "OrderQueryAPI", description: "订单查询接口", type: "api", source_id: "2", schema_definition: null, security_level: "internal", quality_score: 92, usage_count: 56, owner_id: "2", tags: ["api"], created_at: "", updated_at: "" },
  { id: "12", guid: "api-002", name: "CustomerInsightAPI", description: "客户洞察接口", type: "api", source_id: "2", schema_definition: null, security_level: "sensitive", quality_score: 90, usage_count: 45, owner_id: "2", tags: ["api"], created_at: "", updated_at: "" },
];

const mockLineageEdges: LineageEdge[] = [
  { id: "e1", source_asset_id: "1", target_asset_id: "4", transformation_type: "CDC", transformation_detail: "Canal实时同步", created_at: "" },
  { id: "e2", source_asset_id: "2", target_asset_id: "5", transformation_type: "CDC", transformation_detail: "Canal实时同步", created_at: "" },
  { id: "e3", source_asset_id: "3", target_asset_id: "5", transformation_type: "Merge", transformation_detail: "数据合并", created_at: "" },
  { id: "e4", source_asset_id: "4", target_asset_id: "6", transformation_type: "ETL", transformation_detail: "订单数据清洗", created_at: "" },
  { id: "e5", source_asset_id: "5", target_asset_id: "7", transformation_type: "ETL", transformation_detail: "客户画像构建", created_at: "" },
  { id: "e6", source_asset_id: "6", target_asset_id: "8", transformation_type: "Aggregation", transformation_detail: "按天汇总", created_at: "" },
  { id: "e7", source_asset_id: "8", target_asset_id: "9", transformation_type: "Calculation", transformation_detail: "收入计算", created_at: "" },
  { id: "e8", source_asset_id: "7", target_asset_id: "10", transformation_type: "Analysis", transformation_detail: "客户分析", created_at: "" },
  { id: "e9", source_asset_id: "6", target_asset_id: "11", transformation_type: "API", transformation_detail: "实时查询", created_at: "" },
  { id: "e10", source_asset_id: "7", target_asset_id: "12", transformation_type: "API", transformation_detail: "画像查询", created_at: "" },
];

// Layout positions for visualization
const layoutPositions: Record<string, { x: number; y: number }> = {
  "1": { x: 0, y: 0 },
  "2": { x: 0, y: 100 },
  "3": { x: 0, y: 200 },
  "4": { x: 200, y: 50 },
  "5": { x: 200, y: 150 },
  "6": { x: 400, y: 50 },
  "7": { x: 400, y: 150 },
  "8": { x: 600, y: 50 },
  "9": { x: 800, y: 25 },
  "10": { x: 800, y: 100 },
  "11": { x: 600, y: 150 },
  "12": { x: 600, y: 250 },
};

const typeIcons: Record<string, typeof Database> = {
  table: Table2,
  file: FileText,
  api: Api,
  metric: BarChart3,
  dashboard: LayoutDashboard,
};

const typeColors: Record<string, string> = {
  table: "bg-blue-100 border-blue-300 text-blue-700",
  file: "bg-gray-100 border-gray-300 text-gray-700",
  api: "bg-green-100 border-green-300 text-green-700",
  metric: "bg-purple-100 border-purple-300 text-purple-700",
  dashboard: "bg-orange-100 border-orange-300 text-orange-700",
};

const layers = [
  { name: "源数据层", nodes: ["1", "2", "3"] },
  { name: "ODS层", nodes: ["4", "5"] },
  { name: "DWD层", nodes: ["6", "7"] },
  { name: "DWS层", nodes: ["8"] },
  { name: "ADS层", nodes: ["9", "10", "11", "12"] },
];

export default function LineagePage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [selectedNode, setSelectedNode] = useState<string | null>(null);
  const [hoveredNode, setHoveredNode] = useState<string | null>(null);
  const [zoom, setZoom] = useState(1);
  const [panOffset, setPanOffset] = useState({ x: 0, y: 0 });

  const selectedNodeData = selectedNode
    ? mockLineageNodes.find((n) => n.id === selectedNode)
    : null;

  const hoveredNodeData = hoveredNode
    ? mockLineageNodes.find((n) => n.id === hoveredNode)
    : null;

  const filteredNodes = searchKeyword
    ? mockLineageNodes.filter(
        (n) =>
          n.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          n.description.toLowerCase().includes(searchKeyword.toLowerCase())
      )
    : mockLineageNodes;

  const getConnectedNodes = (nodeId: string) => {
    const connected: string[] = [];
    mockLineageEdges.forEach((edge) => {
      if (edge.source_asset_id === nodeId) {
        connected.push(edge.target_asset_id);
      }
      if (edge.target_asset_id === nodeId) {
        connected.push(edge.source_asset_id);
      }
    });
    return connected;
  };

  return (
    <MainLayout>
      <div className="flex h-[calc(100vh-4rem)]">
        {/* Main Content */}
        <div className="flex-1 flex flex-col">
          {/* Header */}
          <div className="p-4 border-b bg-background">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-xl font-bold flex items-center gap-2">
                  <GitBranch className="h-5 w-5" />
                  数据血缘图
                </h1>
                <p className="text-sm text-muted-foreground">
                  追踪数据从源头到应用的全链路血缘关系
                </p>
              </div>
              <div className="flex items-center gap-2">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="搜索节点..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="pl-9 w-64"
                  />
                </div>
                <Select defaultValue="all">
                  <SelectTrigger className="w-32">
                    <SelectValue placeholder="资产类型" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部类型</SelectItem>
                    <SelectItem value="table">数据表</SelectItem>
                    <SelectItem value="api">API接口</SelectItem>
                    <SelectItem value="metric">指标</SelectItem>
                  </SelectContent>
                </Select>
                <Button variant="outline" size="sm" className="gap-2">
                  <Filter className="h-4 w-4" />
                  筛选
                </Button>
              </div>
            </div>
          </div>

          {/* Layer Legend */}
          <div className="px-4 py-2 border-b bg-muted/30">
            <div className="flex items-center gap-6">
              {layers.map((layer, index) => (
                <div key={layer.name} className="flex items-center gap-2">
                  <div
                    className="h-6 w-2 rounded-full bg-gradient-to-b from-primary/50 to-primary/20"
                    style={{ minHeight: `${layer.nodes.length * 20}px` }}
                  />
                  <div>
                    <p className="text-xs font-medium">{layer.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {layer.nodes.length} 个节点
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Lineage Canvas */}
          <div className="flex-1 relative overflow-hidden bg-gradient-to-br from-muted/20 to-muted/5">
            {/* Zoom Controls */}
            <div className="absolute top-4 right-4 flex flex-col gap-2 z-10">
              <Button
                variant="outline"
                size="icon"
                className="bg-background"
                onClick={() => setZoom((z) => Math.min(z + 0.1, 2))}
              >
                <ZoomIn className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                size="icon"
                className="bg-background"
                onClick={() => setZoom((z) => Math.max(z - 0.1, 0.5))}
              >
                <ZoomOut className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                size="icon"
                className="bg-background"
                onClick={() => {
                  setZoom(1);
                  setPanOffset({ x: 0, y: 0 });
                }}
              >
                <Maximize2 className="h-4 w-4" />
              </Button>
            </div>

            {/* SVG Canvas */}
            <div
              className="w-full h-full"
              style={{
                transform: `scale(${zoom}) translate(${panOffset.x}px, ${panOffset.y}px)`,
                transformOrigin: "center center",
              }}
            >
              <svg
                className="w-full h-full"
                viewBox="0 0 1000 400"
                preserveAspectRatio="xMidYMid meet"
              >
                {/* Edges */}
                <g className="edges">
                  {mockLineageEdges.map((edge) => {
                    const source = layoutPositions[edge.source_asset_id];
                    const target = layoutPositions[edge.target_asset_id];
                    if (!source || !target) return null;

                    const isHighlighted =
                      selectedNode === edge.source_asset_id ||
                      selectedNode === edge.target_asset_id ||
                      hoveredNode === edge.source_asset_id ||
                      hoveredNode === edge.target_asset_id;

                    return (
                      <g key={edge.id}>
                        <path
                          d={`M ${source.x + 80} ${source.y + 20} 
                              C ${source.x + 150} ${source.y + 20},
                                ${target.x - 50} ${target.y + 20},
                                ${target.x} ${target.y + 20}`}
                          fill="none"
                          stroke={isHighlighted ? "#3b82f6" : "#d1d5db"}
                          strokeWidth={isHighlighted ? 2.5 : 1.5}
                          strokeDasharray={edge.transformation_type === "CDC" ? "5,5" : undefined}
                          markerEnd="url(#arrowhead)"
                        />
                        <text
                          x={(source.x + target.x) / 2 + 40}
                          y={(source.y + target.y) / 2 + 10}
                          className="text-[8px] fill-muted-foreground"
                          textAnchor="middle"
                        >
                          {edge.transformation_type}
                        </text>
                      </g>
                    );
                  })}
                  <defs>
                    <marker
                      id="arrowhead"
                      markerWidth="6"
                      markerHeight="6"
                      refX="5"
                      refY="3"
                      orient="auto"
                    >
                      <polygon
                        points="0 0, 6 3, 0 6"
                        fill="#9ca3af"
                      />
                    </marker>
                  </defs>
                </g>

                {/* Nodes */}
                <g className="nodes">
                  {filteredNodes.map((node) => {
                    const pos = layoutPositions[node.id];
                    if (!pos) return null;

                    const Icon = typeIcons[node.type] || Database;
                    const isSelected = selectedNode === node.id;
                    const isHovered = hoveredNode === node.id;
                    const isConnected =
                      selectedNode && getConnectedNodes(selectedNode).includes(node.id);
                    const isFaded =
                      (selectedNode && !isSelected && !isConnected) ||
                      (hoveredNode && !isSelected && !isConnected);

                    return (
                      <g
                        key={node.id}
                        transform={`translate(${pos.x}, ${pos.y})`}
                        className="cursor-pointer"
                        onClick={() =>
                          setSelectedNode(isSelected ? null : node.id)
                        }
                        onMouseEnter={() => setHoveredNode(node.id)}
                        onMouseLeave={() => setHoveredNode(null)}
                      >
                        <rect
                          x="0"
                          y="0"
                          width="160"
                          height="40"
                          rx="6"
                          className={cn(
                            "transition-all duration-200",
                            typeColors[node.type],
                            isSelected && "stroke-primary stroke-2",
                            isHovered && !isSelected && "stroke-primary stroke-2",
                            isConnected && !isSelected && "stroke-primary/50",
                            isFaded && "opacity-30"
                          )}
                          fill="white"
                        />
                        <foreignObject x="8" y="8" width="24" height="24">
                          <div className="flex items-center justify-center h-full">
                            <Icon className="h-4 w-4" />
                          </div>
                        </foreignObject>
                        <text
                          x="40"
                          y="16"
                          className="text-[11px] font-medium fill-foreground"
                        >
                          {node.name.length > 15
                            ? node.name.slice(0, 15) + "..."
                            : node.name}
                        </text>
                        <text
                          x="40"
                          y="30"
                          className="text-[9px] fill-muted-foreground"
                        >
                          {node.type === "table" ? "数据表" : node.type === "api" ? "API" : "指标"}
                        </text>
                      </g>
                    );
                  })}
                </g>
              </svg>
            </div>

            {/* Hover Tooltip */}
            {hoveredNodeData && (
              <div className="absolute bottom-4 left-4 bg-background border rounded-lg shadow-lg p-4 max-w-xs animate-fade-in">
                <div className="flex items-center gap-2 mb-2">
                  {(() => {
                    const Icon = typeIcons[hoveredNodeData.type] || Database;
                    return <Icon className="h-5 w-5 text-primary" />;
                  })()}
                  <span className="font-semibold">{hoveredNodeData.name}</span>
                </div>
                <p className="text-sm text-muted-foreground mb-2">
                  {hoveredNodeData.description}
                </p>
                <div className="flex items-center gap-3 text-xs">
                  <SecurityBadge level={hoveredNodeData.security_level} />
                  <span>质量: {hoveredNodeData.quality_score}</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Sidebar - Node Detail */}
        <div className="w-80 border-l bg-background overflow-y-auto">
          {selectedNodeData ? (
            <div className="p-4 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold">节点详情</h3>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setSelectedNode(null)}
                >
                  关闭
                </Button>
              </div>

              <Card>
                <CardContent className="pt-4">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
                      {(() => {
                        const Icon = typeIcons[selectedNodeData.type] || Database;
                        return <Icon className="h-6 w-6 text-primary" />;
                      })()}
                    </div>
                    <div>
                      <h4 className="font-semibold">{selectedNodeData.name}</h4>
                      <p className="text-sm text-muted-foreground">
                        {selectedNodeData.type === "table"
                          ? "数据表"
                          : selectedNodeData.type === "api"
                          ? "API接口"
                          : "指标"}
                      </p>
                    </div>
                  </div>
                  <p className="text-sm">{selectedNodeData.description}</p>
                  <div className="flex items-center gap-2 mt-3">
                    <SecurityBadge level={selectedNodeData.security_level} />
                    <QualityScore
                      score={selectedNodeData.quality_score}
                      size="sm"
                    />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">上下游节点</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {/* Upstream */}
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">
                      <ChevronLeft className="h-3 w-3 inline" /> 上游节点
                    </p>
                    {mockLineageEdges
                      .filter((e) => e.target_asset_id === selectedNode)
                      .map((edge) => {
                        const upstream = mockLineageNodes.find(
                          (n) => n.id === edge.source_asset_id
                        );
                        return upstream ? (
                          <Link
                            key={edge.id}
                            href={`/asset/${upstream.id}`}
                            className="block p-2 rounded hover:bg-muted transition-colors"
                          >
                            <p className="text-sm font-medium flex items-center gap-1">
                              <ChevronLeft className="h-3 w-3" />
                              {upstream.name}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {edge.transformation_detail}
                            </p>
                          </Link>
                        ) : null;
                      })}
                  </div>
                  {/* Downstream */}
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">
                      下游节点 <ChevronRight className="h-3 w-3 inline" />
                    </p>
                    {mockLineageEdges
                      .filter((e) => e.source_asset_id === selectedNode)
                      .map((edge) => {
                        const downstream = mockLineageNodes.find(
                          (n) => n.id === edge.target_asset_id
                        );
                        return downstream ? (
                          <Link
                            key={edge.id}
                            href={`/asset/${downstream.id}`}
                            className="block p-2 rounded hover:bg-muted transition-colors"
                          >
                            <p className="text-sm font-medium flex items-center gap-1">
                              {downstream.name}
                              <ChevronRight className="h-3 w-3" />
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {edge.transformation_detail}
                            </p>
                          </Link>
                        ) : null;
                      })}
                  </div>
                </CardContent>
              </Card>

              <Button className="w-full gap-2" asChild>
                <Link href={`/asset/${selectedNode}`}>
                  查看详情
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </Button>
            </div>
          ) : (
            <div className="p-4 text-center text-muted-foreground">
              <GitBranch className="h-12 w-12 mx-auto mb-3 opacity-30" />
              <p className="text-sm">点击节点查看详情</p>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
}
