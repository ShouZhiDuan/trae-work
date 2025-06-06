# CSV大数据生成工具使用说明

本工具提供了两种方式来生成基于 `sample-data.csv` 格式的大规模测试数据。

## 📁 文件说明

- `generate_large_csv.py` - Python生成脚本（独立使用）
- `generate_csv.sh` - Shell包装脚本（推荐使用）
- `CSV_GENERATOR_README.md` - 本说明文档

## 🚀 快速开始

### 方法一：使用Shell脚本（推荐）

```bash
# 生成100万行数据（默认）
./generate_csv.sh

# 生成指定行数的数据
./generate_csv.sh 500000

# 指定行数和输出文件名
./generate_csv.sh 2000000 my_large_data.csv
```

### 方法二：直接使用Python脚本

```bash
# 使用默认配置（100万行）
python3 generate_large_csv.py

# 修改脚本中的参数后运行
# 编辑 TOTAL_ROWS 和 OUTPUT_FILE 变量
```

## 📊 生成的数据格式

生成的CSV文件包含以下字段：

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | 整数 | 唯一标识符 | 1, 2, 3... |
| name | 字符串 | 中文姓名 | 张三, 李四 |
| age | 整数 | 年龄(22-65) | 25, 30, 35 |
| salary | 浮点数 | 薪资(30000-500000) | 50000.50, 60000.75 |
| is_active | 布尔值 | 是否激活 | true, false |
| created_date | 日期 | 创建日期(2020-2024) | 2023-01-01 |
| email | 字符串 | 邮箱地址 | user1@example.com |
| department | 字符串 | 部门名称 | 技术部, 销售部 |

## ⚙️ 配置参数

### Python脚本配置

编辑 `generate_large_csv.py` 中的以下参数：

```python
TOTAL_ROWS = 1000000      # 生成行数
OUTPUT_FILE = "large_sample_data.csv"  # 输出文件名
BATCH_SIZE = 10000        # 批处理大小
```

### 数据池自定义

可以修改以下数据池来自定义生成的数据：

```python
CHINESE_SURNAMES = ["张", "李", "王", ...]  # 姓氏
CHINESE_NAMES = ["伟", "芳", "娜", ...]     # 名字
DEPARTMENTS = ["技术部", "销售部", ...]      # 部门
EMAIL_DOMAINS = ["example.com", ...]        # 邮箱域名
```

## 📈 性能说明

### 生成速度
- **小数据量** (< 10万行): 几秒钟
- **中等数据量** (10-100万行): 1-5分钟
- **大数据量** (> 100万行): 5-30分钟

### 文件大小估算
- **每行约100字节**
- **100万行 ≈ 100MB**
- **1000万行 ≈ 1GB**

### 内存使用
- 使用批处理机制，内存占用稳定在50MB以内
- 支持生成任意大小的文件而不会内存溢出

## 🔧 使用示例

### 生成不同规模的测试数据

```bash
# 小规模测试（1万行）
./generate_csv.sh 10000 small_test.csv

# 中等规模测试（50万行）
./generate_csv.sh 500000 medium_test.csv

# 大规模测试（500万行）
./generate_csv.sh 5000000 large_test.csv
```

### 与CSV导入工具配合使用

```bash
# 1. 生成测试数据
./generate_csv.sh 1000000 test_data.csv

# 2. 导入到MySQL数据库
java -jar target/csv-import-1.0.0.jar \
  test_data.csv \
  "jdbc:mysql://localhost:3306/testdb" \
  root \
  password \
  "id,email" \
  5000 \
  2000
```

## 🛠️ 故障排除

### 常见问题

1. **权限错误**
   ```bash
   chmod +x generate_csv.sh
   chmod +x generate_large_csv.py
   ```

2. **Python未安装**
   ```bash
   # macOS
   brew install python3
   
   # Ubuntu/Debian
   sudo apt-get install python3
   ```

3. **磁盘空间不足**
   - 检查可用空间：`df -h`
   - 减少生成行数或清理磁盘空间

4. **编码问题**
   - 确保终端支持UTF-8编码
   - 生成的文件使用UTF-8编码

### 性能优化建议

1. **SSD硬盘**：使用SSD可显著提升写入速度
2. **调整批处理大小**：根据内存情况调整 `BATCH_SIZE`
3. **并行生成**：可以分段生成后合并文件

## 📝 自定义扩展

### 添加新字段

在Python脚本中修改 `generate_row()` 函数：

```python
def generate_row(row_id):
    # 现有字段...
    
    # 添加新字段
    phone = f"1{random.randint(3000000000, 9999999999)}"
    address = f"{random.choice(['北京', '上海', '广州'])}市"
    
    return [
        row_id, name, age, salary, is_active, 
        created_date, email, department,
        phone, address  # 新字段
    ]
```

### 修改数据分布

```python
# 调整薪资分布
if random.random() < 0.6:  # 60%低薪
    salary = round(random.uniform(30000, 80000), 2)
elif random.random() < 0.9:  # 30%中薪
    salary = round(random.uniform(80000, 150000), 2)
else:  # 10%高薪
    salary = round(random.uniform(150000, 500000), 2)
```

## 🎯 最佳实践

1. **测试先行**：先生成小量数据测试导入流程
2. **分批处理**：大文件可以分成多个小文件处理
3. **备份重要数据**：生成前备份现有数据
4. **监控资源**：注意磁盘空间和生成时间
5. **验证数据**：生成后检查数据格式和完整性

---

**提示**：生成的数据仅用于测试目的，请勿用于生产环境。