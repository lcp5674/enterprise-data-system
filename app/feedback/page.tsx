"use client";

import { useState } from "react";
import { MainLayout } from "@/components/layout/main-layout";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  MessageSquare,
  Star,
  Send,
  CheckCircle,
  Bug,
  Lightbulb,
  HelpCircle,
  ThumbsUp,
  ThumbsDown,
} from "lucide-react";
import { cn } from "@/lib/utils";

type FeedbackCategory = "bug" | "feature" | "general";

interface FeedbackItem {
  id: string;
  user: string;
  content: string;
  rating: number;
  category: FeedbackCategory;
  createdAt: string;
  status: "pending" | "reviewed" | "resolved";
}

const mockFeedbackList: FeedbackItem[] = [
  {
    id: "1",
    user: "张三",
    content: "建议增加数据血缘的批量导出功能，方便在会议中展示。",
    rating: 5,
    category: "feature",
    createdAt: "2024-04-08 14:30",
    status: "reviewed",
  },
  {
    id: "2",
    user: "李四",
    content: "资产搜索的结果分页加载有些慢，希望能优化一下。",
    rating: 3,
    category: "bug",
    createdAt: "2024-04-07 10:20",
    status: "resolved",
  },
  {
    id: "3",
    user: "王五",
    content: "整体使用体验不错，特别是数据地图的分类功能很实用！",
    rating: 5,
    category: "general",
    createdAt: "2024-04-06 16:45",
    status: "resolved",
  },
];

const categoryConfig = {
  bug: {
    icon: Bug,
    label: "问题反馈",
    color: "text-red-600 bg-red-50",
  },
  feature: {
    icon: Lightbulb,
    label: "功能建议",
    color: "text-amber-600 bg-amber-50",
  },
  general: {
    icon: HelpCircle,
    label: "其他意见",
    color: "text-blue-600 bg-blue-50",
  },
};

const statusConfig = {
  pending: { label: "待处理", color: "text-amber-600 bg-amber-50" },
  reviewed: { label: "已查看", color: "text-blue-600 bg-blue-50" },
  resolved: { label: "已解决", color: "text-emerald-600 bg-emerald-50" },
};

export default function FeedbackPage() {
  const [category, setCategory] = useState<FeedbackCategory>("general");
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Simulate submission
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSubmitted(true);
      setContent("");
      setRating(5);
      setCategory("general");
    }, 1500);
  };

  return (
    <MainLayout>
      <div className="container mx-auto p-6 space-y-6 max-w-5xl">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <MessageSquare className="h-6 w-6" />
            反馈中心
          </h1>
          <p className="text-muted-foreground">
            分享您的想法，帮助我们改进产品
          </p>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* Feedback Form */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>提交反馈</CardTitle>
                <CardDescription>
                  您的反馈将帮助我们不断改进产品体验
                </CardDescription>
              </CardHeader>
              <CardContent>
                {isSubmitted ? (
                  <div className="flex flex-col items-center justify-center py-8 space-y-4">
                    <div className="h-16 w-16 rounded-full bg-emerald-100 flex items-center justify-center">
                      <CheckCircle className="h-8 w-8 text-emerald-600" />
                    </div>
                    <h3 className="text-xl font-semibold">感谢您的反馈！</h3>
                    <p className="text-muted-foreground text-center">
                      我们已收到您的反馈，会尽快处理并持续改进产品。
                    </p>
                    <Button
                      variant="outline"
                      onClick={() => setIsSubmitted(false)}
                    >
                      继续反馈
                    </Button>
                  </div>
                ) : (
                  <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Category Selection */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium">反馈类型</label>
                      <div className="grid grid-cols-3 gap-2">
                        {(["bug", "feature", "general"] as FeedbackCategory[]).map(
                          (cat) => {
                            const config = categoryConfig[cat];
                            const Icon = config.icon;
                            return (
                              <button
                                key={cat}
                                type="button"
                                onClick={() => setCategory(cat)}
                                className={cn(
                                  "flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all",
                                  category === cat
                                    ? "border-primary bg-primary/5"
                                    : "border-border hover:border-primary/50"
                                )}
                              >
                                <div
                                  className={cn(
                                    "p-2 rounded-lg",
                                    category === cat
                                      ? config.color
                                      : "bg-muted"
                                  )}
                                >
                                  <Icon className="h-5 w-5" />
                                </div>
                                <span className="text-sm font-medium">
                                  {config.label}
                                </span>
                              </button>
                            );
                          }
                        )}
                      </div>
                    </div>

                    {/* Rating */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium">满意度评分</label>
                      <div className="flex items-center gap-2">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <button
                            key={star}
                            type="button"
                            onClick={() => setRating(star)}
                            className="p-1 hover:scale-110 transition-transform"
                          >
                            <Star
                              className={cn(
                                "h-8 w-8",
                                star <= rating
                                  ? "text-amber-500 fill-amber-500"
                                  : "text-muted-foreground/30"
                              )}
                            />
                          </button>
                        ))}
                        <span className="ml-2 text-sm text-muted-foreground">
                          {rating === 5 && "非常满意"}
                          {rating === 4 && "满意"}
                          {rating === 3 && "一般"}
                          {rating === 2 && "不太满意"}
                          {rating === 1 && "不满意"}
                        </span>
                      </div>
                    </div>

                    {/* Content */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium">反馈内容</label>
                      <Textarea
                        placeholder="请详细描述您的问题或建议..."
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        className="min-h-[120px]"
                        required
                      />
                      <p className="text-xs text-muted-foreground">
                        请尽量详细描述您遇到的问题或建议，方便我们更好地理解和解决。
                      </p>
                    </div>

                    {/* Contact (optional) */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium">
                        联系邮箱（选填）
                      </label>
                      <Input
                        type="email"
                        placeholder="如需回复，请留下您的邮箱"
                      />
                    </div>

                    <Button
                      type="submit"
                      className="w-full gap-2"
                      disabled={isSubmitting || !content}
                    >
                      {isSubmitting ? (
                        "提交中..."
                      ) : (
                        <>
                          <Send className="h-4 w-4" />
                          提交反馈
                        </>
                      )}
                    </Button>
                  </form>
                )}
              </CardContent>
            </Card>

            {/* NPS Survey */}
            <Card className="mt-6">
              <CardHeader>
                <CardTitle className="text-base">产品体验评分</CardTitle>
                <CardDescription>
                  您有多大可能向同事推荐我们的产品？
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between gap-4">
                  <ThumbsDown className="h-5 w-5 text-muted-foreground" />
                  <div className="flex-1 flex justify-between">
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((n) => (
                      <button
                        key={n}
                        className="h-10 w-10 rounded-full border hover:border-primary hover:bg-primary/5 transition-colors font-medium"
                      >
                        {n}
                      </button>
                    ))}
                  </div>
                  <ThumbsUp className="h-5 w-5 text-emerald-600" />
                </div>
                <p className="text-xs text-muted-foreground text-center mt-3">
                  1 = 完全不可能 &nbsp;&nbsp;&nbsp;&nbsp; 10 = 非常可能
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Feedback List */}
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">近期反馈</CardTitle>
                <CardDescription>查看其他用户的反馈和建议</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {mockFeedbackList.map((item) => {
                  const categoryInfo = categoryConfig[item.category];
                  const statusInfo = statusConfig[item.status];
                  const Icon = categoryInfo.icon;

                  return (
                    <div
                      key={item.id}
                      className="p-3 rounded-lg border bg-card hover:bg-muted/50 transition-colors"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <div
                            className={cn(
                              "p-1 rounded",
                              item.category === "bug"
                                ? "bg-red-100 text-red-600"
                                : item.category === "feature"
                                ? "bg-amber-100 text-amber-600"
                                : "bg-blue-100 text-blue-600"
                            )}
                          >
                            <Icon className="h-3 w-3" />
                          </div>
                          <span className="text-sm font-medium">{item.user}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          {Array.from({ length: 5 }).map((_, i) => (
                            <Star
                              key={i}
                              className={cn(
                                "h-3 w-3",
                                i < item.rating
                                  ? "text-amber-500 fill-amber-500"
                                  : "text-muted-foreground/30"
                              )}
                            />
                          ))}
                        </div>
                      </div>
                      <p className="text-sm text-muted-foreground mb-2">
                        {item.content}
                      </p>
                      <div className="flex items-center justify-between">
                        <span className="text-xs text-muted-foreground">
                          {item.createdAt}
                        </span>
                        <span
                          className={cn(
                            "text-xs px-2 py-0.5 rounded-full",
                            statusInfo.color
                          )}
                        >
                          {statusInfo.label}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </CardContent>
            </Card>

            {/* Quick Links */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">帮助资源</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <Button variant="outline" className="w-full justify-start gap-2">
                  <HelpCircle className="h-4 w-4" />
                  使用文档
                </Button>
                <Button variant="outline" className="w-full justify-start gap-2">
                  <MessageSquare className="h-4 w-4" />
                  在线客服
                </Button>
                <Button variant="outline" className="w-full justify-start gap-2">
                  <Bug className="h-4 w-4" />
                  常见问题
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
