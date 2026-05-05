#!/bin/bash

# Elasticsearch 索引创建脚本
# 使用前请确认：
# 1. Elasticsearch 服务已启动
# 2. analysis-ik 插件已安装
# 3. 修改 ES_HOST 和 ES_PASSWORD 为你的实际配置

# Elasticsearch 配置
ES_HOST="http://xxxxx:9200"
ES_USER="elastic"
ES_PASSWORD="xxxxxx"
INDEX_NAME="entries_index_local"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Elasticsearch 索引创建脚本${NC}"
echo -e "${YELLOW}========================================${NC}"

# 检查 Elasticsearch 连接
echo -e "\n${YELLOW}[1/4] 检查 Elasticsearch 连接...${NC}"
ES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -u "${ES_USER}:${ES_PASSWORD}" "${ES_HOST}")

if [ "$ES_STATUS" != "200" ]; then
    echo -e "${RED}❌ 无法连接到 Elasticsearch (HTTP ${ES_STATUS})${NC}"
    echo -e "${RED}请检查:${NC}"
    echo -e "  - Elasticsearch 服务是否运行"
    echo -e "  - 主机地址和密码是否正确"
    exit 1
fi
echo -e "${GREEN}✅ Elasticsearch 连接成功${NC}"

# 检查 IK 分词器插件
echo -e "\n${YELLOW}[2/4] 检查 IK 分词器插件...${NC}"
PLUGINS=$(curl -s -u "${ES_USER}:${ES_PASSWORD}" "${ES_HOST}/_cat/plugins?v" 2>/dev/null)
if echo "$PLUGINS" | grep -q "analysis-ik"; then
    echo -e "${GREEN}✅ IK 分词器已安装${NC}"
else
    echo -e "${RED}⚠️  警告: 未检测到 IK 分词器插件${NC}"
    echo -e "${YELLOW}如需安装，请执行:${NC}"
    echo -e "  docker exec -it 1Panel-elasticsearch-3hJV ./bin/elasticsearch-plugin install analysis-ik"
    read -p "是否继续创建索引? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 删除已存在的索引（如果存在）
echo -e "\n${YELLOW}[3/4] 检查现有索引...${NC}"
INDEX_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" -u "${ES_USER}:${ES_PASSWORD}" "${ES_HOST}/${INDEX_NAME}")

if [ "$INDEX_EXISTS" == "200" ]; then
    echo -e "${YELLOW}⚠️  索引 '${INDEX_NAME}' 已存在${NC}"
    read -p "是否删除并重新创建? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}删除索引...${NC}"
        curl -X DELETE -u "${ES_USER}:${ES_PASSWORD}" "${ES_HOST}/${INDEX_NAME}"
        echo -e "${GREEN}✅ 索引已删除${NC}"
    else
        echo -e "${YELLOW}取消操作${NC}"
        exit 0
    fi
fi

# 创建索引
echo -e "\n${YELLOW}[4/4] 创建索引 '${INDEX_NAME}'...${NC}"

CREATE_RESPONSE=$(curl -X PUT "${ES_HOST}/${INDEX_NAME}" \
  -u "${ES_USER}:${ES_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "userId": {
        "type": "long",
        "index": true
      },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart",
        "fields": {
          "raw": {
            "type": "keyword",
            "ignore_above": 512
          }
        }
      },
      "entryType": {
        "type": "keyword"
      },
      "checked": {
        "type": "boolean"
      },
      "completionRate": {
        "type": "integer",
        "index": true
      },
      "createTime": {
        "type": "date"
      },
      "updateTime": {
        "type": "date"
      },
      "isDelete": {
        "type": "boolean",
        "index": true
      }
    }
  }
}')

echo "$CREATE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CREATE_RESPONSE"

if echo "$CREATE_RESPONSE" | grep -q '"acknowledged":true'; then
    echo -e "\n${GREEN}✅ 索引创建成功!${NC}"
else
    echo -e "\n${RED}❌ 索引创建失败${NC}"
    exit 1
fi

# 验证索引
echo -e "\n${YELLOW}验证索引映射...${NC}"
curl -s -u "${ES_USER}:${ES_PASSWORD}" "${ES_HOST}/${INDEX_NAME}/_mapping" | python3 -m json.tool 2>/dev/null

# 测试分词
echo -e "\n${YELLOW}测试中文分词效果...${NC}"
curl -s -X POST "${ES_HOST}/${INDEX_NAME}/_analyze" \
  -u "${ES_USER}:${ES_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{
  "analyzer": "chinese_analyzer",
  "text": "今天完成了项目开发的思考"
}' | python3 -m json.tool 2>/dev/null

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}所有操作完成!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}常用命令:${NC}"
echo -e "  查看索引: curl -u ${ES_USER}:${ES_PASSWORD} ${ES_HOST}/_cat/indices?v"
echo -e "  查看映射: curl -u ${ES_USER}:${ES_PASSWORD} ${ES_HOST}/${INDEX_NAME}/_mapping?pretty"
echo -e "  删除索引: curl -X DELETE -u ${ES_USER}:${ES_PASSWORD} ${ES_HOST}/${INDEX_NAME}"
