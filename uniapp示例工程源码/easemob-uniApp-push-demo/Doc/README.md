# 推送集成文档目录

本目录包含各平台推送服务的详细集成指南和关键信息说明。

## 目录结构

```
Doc/
├── README.md                    # 本文档，目录说明
├── OPPO/                       # OPPO推送集成文档
│   ├── oppo_push_integration_guide.md   # OPPO推送详细集成步骤
│   └── key_screenshots.md               # OPPO推送配置的关键截图和验证步骤
├── Xiaomi/                     # 小米推送集成文档
│   ├── xiaomi_push_integration_guide.md # 小米推送详细集成步骤
│   └── key_screenshots.md                 # 小米推送配置的关键截图和验证步骤
├── VIVO/                       # VIVO推送集成文档
│   ├── vivo_push_integration_guide.md   # VIVO推送详细集成步骤
│   └── key_screenshots.md                 # VIVO推送配置的关键截图和验证步骤
└── [其他平台]/                  # 后续可添加其他平台文档
```

## 文档链接

- [OPPO 推送集成指南](OPPO/oppo_push_integration_guide.md)
- [OPPO 关键截图说明](OPPO/key_screenshots.md)
- [小米推送集成指南](Xiaomi/xiaomi_push_integration_guide.md)
- [小米关键截图说明](Xiaomi/key_screenshots.md)
- [VIVO 推送集成指南](VIVO/vivo_push_integration_guide.md)
- [VIVO 关键截图说明](VIVO/key_screenshots.md)

## 使用说明

### 1. 快速开始
- 根据目标平台选择对应目录
- 阅读平台特定的集成指南
- 参考关键截图进行配置验证

### 2. 文档规范
- 每个平台包含：
  - 详细集成指南（`.md`格式）
  - 关键截图说明（`.md`格式）
  - 相关截图文件（保存在`static/demo/`）

### 3. 截图管理
- 所有截图统一存放在`static/demo/`目录
- 按照`[平台]_[功能]_[状态]_[时间戳].png`格式命名
- 敏感信息需要打码处理

## 平台支持计划

| 平台 | 状态 | 预计完成时间 |
|------|------|--------------|
| OPPO | ✅ 已完成 | 2025-03-04 |
| 小米 | ✅ 已完成 | 2025-03-04 |
| 华为 | 🚧 待添加 | 待定 |
| VIVO | ✅ 已完成 | 2026-04-29 |
| 魅族 | 🚧 待添加 | 待定 |

## 更新日志

### 2026-04-29
- 添加VIVO平台推送集成指南
- 添加VIVO关键截图说明文档

### 2025-03-04
- 创建Doc目录结构
- 添加OPPO平台推送集成指南
- 添加OPPO关键截图说明文档

## 贡献指南

如需添加新的平台文档：
1. 创建对应平台目录（如`Xiaomi/`）
2. 按照现有格式创建集成指南
3. 添加对应的关键截图说明
4. 更新本文档的目录结构和平台支持计划

## 联系方式

如有问题或建议，请联系项目维护人员。