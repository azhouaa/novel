<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <div class="my_l">
        <ul class="log_list">
          <li>
            <router-link class="link_4" :to="{ name: 'authorBookUpload' }">上传小说</router-link>
          </li>
          <li>
            <router-link class="link_4 on" :to="{ name: 'authorUploadRecords' }">上传记录</router-link>
          </li>
        </ul>
      </div>
      <div class="my_r">
        <div class="my_bookshelf">
          <div class="title cf">
            <h2 class="fl">上传记录（全部用户）</h2>
            <div class="fr">
              <router-link :to="{ name: 'authorBookUpload' }" class="btn_red">继续上传</router-link>
            </div>
          </div>
          <div class="no-data" v-if="total === 0">暂无上传记录。</div>
          <div id="divData" class="updateTable">
            <table cellpadding="0" cellspacing="0">
              <thead>
                <tr>
                  <th class="goread">用户ID</th>
                  <th class="goread">小说名</th>
                  <th class="goread">作者</th>
                  <th class="goread">TXT文件</th>
                  <th class="goread">章节数</th>
                  <th class="goread">总字数</th>
                  <th class="goread">编码</th>
                  <th class="goread">耗时</th>
                  <th class="goread">状态</th>
                  <th class="goread">上传时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in records" :key="index">
                  <td class="goread">{{ item.userId }}</td>
                  <td class="goread">{{ item.bookName }}</td>
                  <td class="goread">{{ item.authorName || "-" }}</td>
                  <td class="goread">{{ item.txtFileName }}</td>
                  <td class="goread">{{ item.chapterTotal }}</td>
                  <td class="goread">{{ item.wordTotal }}</td>
                  <td class="goread">{{ item.txtCharset }}</td>
                  <td class="goread">{{ item.durationMs }}ms</td>
                  <td class="goread">
                    <span class="status-ok" v-if="Number(item.status) === 1">成功</span>
                    <span class="status-fail" v-else>失败</span>
                  </td>
                  <td class="goread">{{ item.createTime }}</td>
                </tr>
              </tbody>
            </table>
            <el-pagination
              small
              layout="prev, pager, next"
              :background="true"
              :page-size="pageSize"
              :total="total"
              class="mt-4"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted } from "vue";
import { listUploadRecords } from "@/api/author";
import AuthorHeader from "@/components/author/Header.vue";

export default {
  name: "authorUploadRecords",
  components: {
    AuthorHeader,
  },
  setup() {
    const state = reactive({
      records: [],
      searchCondition: {},
      total: 0,
      pageSize: 10,
    });

    onMounted(() => {
      load();
    });

    /**
     * 加载上传记录分页数据。
     */
    const load = async () => {
      const { data } = await listUploadRecords(state.searchCondition);
      state.records = data.list;
      state.searchCondition.pageNum = data.pageNum;
      state.searchCondition.pageSize = data.pageSize;
      state.total = Number(data.total);
    };

    /**
     * 分页切换。
     */
    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      load();
    };

    return {
      ...toRefs(state),
      load,
      handleCurrentChange,
    };
  },
};
</script>

<style scoped>
.userBox {
  margin: 0 auto 50px;
  background: #fff;
  border-radius: 6px;
}

.my_l {
  width: 198px;
  float: left;
  font-size: 13px;
  padding-top: 20px;
}

.my_l li a {
  display: block;
  height: 42px;
  line-height: 42px;
  padding-left: 62px;
  border-left: 4px solid #fff;
  margin-bottom: 5px;
  color: #666;
}

.my_l li .on {
  background-color: #fafafa;
  border-left: 2px solid #2196f3;
  color: #000;
  border-radius: 0 2px 2px 0;
}

.my_l .link_4 {
  background-position: 32px -314px;
}

.my_r {
  width: 739px;
  padding: 0 30px 30px;
  float: right;
  border-left: 1px solid #efefef;
  min-height: 470px;
}

.my_bookshelf .title {
  padding: 20px 0 15px;
  line-height: 30px;
}

.my_bookshelf h2 {
  font-size: 18px;
  font-weight: normal;
}

.btn_red {
  border-radius: 20px;
}

.updateTable {
  width: 739px;
  color: #999;
  border-top: 1px solid #f1f1f1;
  animation: fadeInUp 0.35s ease;
}

.updateTable table {
  width: 100%;
  margin-bottom: 14px;
}

.updateTable th,
.updateTable td {
  height: 40px;
  line-height: 40px;
  vertical-align: middle;
  padding-left: 6px;
  font-weight: normal;
  text-align: left;
}

.updateTable th {
  background: #f9f9f9;
  color: #333;
  border-top: 1px solid #eee;
}

.updateTable .goread {
  text-align: center;
}

.updateTable tr:nth-child(2n) td {
  background: #fafafa;
}

.no-data {
  border-top: 1px solid #f1f1f1;
  color: #999;
  text-align: center;
  padding: 80px 0 70px;
  font-size: 14px;
}

.status-ok {
  color: #35a45a;
  font-weight: 600;
}

.status-fail {
  color: #e24a4a;
  font-weight: 600;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
