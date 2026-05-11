#!/usr/bin/env python3

import sys
import os
import argparse

try:
    import pandas as pd
except ImportError:
    pd = None


def install_dependencies():
    print("检查并安装依赖...")
    try:
        import subprocess
        import sys

        deps = ["pandas"]
        for dep in deps:
            try:
                __import__(dep)
                print(f"  ✓ {dep} 已安装")
            except ImportError:
                print(f"  正在安装 {dep}...")
                subprocess.check_call([sys.executable, "-m", "pip", "install", dep, "-q"])
                print(f"  ✓ {dep} 安装完成")

        print("\n所有依赖已安装完成！")
        return True

    except Exception as e:
        print(f"安装依赖失败: {e}")
        return False


def run_tests():
    if pd is None:
        if not install_dependencies():
            print("无法继续运行，依赖安装失败")
            sys.exit(1)
        try:
            import pandas as pd
        except ImportError:
            print("pandas仍不可用")
            sys.exit(1)

    print("EDAMS 性能测试依赖检查")
    print("=" * 40)
    print(f"pandas: ✓")
    print("所有依赖检查通过！")


if __name__ == "__main__":
    install_dependencies()
    run_tests()
