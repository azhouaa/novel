<template>
  <Header />

  <div class="main box_center cf mb50">
    <div class="channelRankingContent cf">
      <div class="wrap_left fl">
        <div class="wrap_bg">
          <!-- 榜单详情 start -->
          <div class="pad20">
            <div class="book_tit">
              <div class="fl">
                <h3 class="font26 mt5 mb5" id="rankName">{{ rankName }}</h3>
              </div>
              <a class="fr rank-export-btn" href="javascript:void(0)" @click="downloadCurrentRank">
                {{ exporting ? '导出中...' : '导出当前榜单' }}
              </a>
            </div>
            <div class="updateTable rankTable">
              <table cellpadding="0" cellspacing="0">
                <thead>
                  <tr>
                    <th class="rank">排名</th>
                    <th class="style">类别</th>
                    <th class="name">书名</th>
                    <th class="chapter">最新章节</th>
                    <th class="author">作者</th>
                    <th class="word">字数</th>
                  </tr>
                </thead>
                <tbody id="bookRankList">
                  <tr v-for="(item, index) in books" :key="item.id || index">
                    <td class="rank">
                      <i :class="'num' + (index + 1)">{{ index + 1 }}</i>
                    </td>
                    <td class="style">
                      <a href="javascript:void(0)" @click="bookDetail(item.id)">[{{ item.categoryName }}]</a>
                    </td>
                    <td class="name">
                      <a @click="bookDetail(item.id)" href="javascript:void(0)">{{ item.bookName }}</a>
                    </td>
                    <td class="chapter">
                      <a @click="bookDetail(item.id)" href="javascript:void(0)">{{ item.lastChapterName }}</a>
                    </td>
                    <td class="author">
                      <a href="javascript:void(0)">{{ item.authorName }}</a>
                    </td>
                    <td class="word">{{ wordCountFormat(item.wordCount) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <!-- 榜单详情 end -->
        </div>
      </div>

      <div class="wrap_right fr">
        <div class="wrap_inner wrap_right_cont mb20">
          <div class="title cf noborder">
            <h3 class="on">排行榜</h3>
          </div>
          <div class="rightList2">
            <ul id="rankType">
              <li>
                <a :class="`${rankType == 1 ? 'on' : ''}`" href="javascript:void(0)" @click="visitRank">点击榜</a>
              </li>
              <li>
                <a :class="`${rankType == 2 ? 'on' : ''}`" href="javascript:void(0)" @click="newestRank">新书榜</a>
              </li>
              <li>
                <a :class="`${rankType == 3 ? 'on' : ''}`" href="javascript:void(0)" @click="updateRank">更新榜</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  listVisitRankBooks,
  listUpdateRankBooks,
  listNewestRankBooks,
  exportRankExcel,
} from "@/api/book";
import { ElMessage } from "element-plus";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";

export default {
  name: "bookRank",
  components: {
    Header,
    Footer,
  },
  setup() {
    const router = useRouter();

    const state = reactive({
      books: [],
      rankName: "点击榜",
      rankType: 1,
      exporting: false,
    });

    onMounted(() => {
      visitRank();
    });

    const visitRank = async () => {
      const { data } = await listVisitRankBooks();
      state.books = data || [];
      state.rankName = "点击榜";
      state.rankType = 1;
    };

    const newestRank = async () => {
      const { data } = await listNewestRankBooks();
      state.books = data || [];
      state.rankName = "新书榜";
      state.rankType = 2;
    };

    const updateRank = async () => {
      const { data } = await listUpdateRankBooks();
      state.books = data || [];
      state.rankName = "更新榜";
      state.rankType = 3;
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    /**
     * 导出当前选中的榜单。
     */
    const downloadCurrentRank = async () => {
      if (state.exporting) {
        return;
      }
      state.exporting = true;
      try {
        const typeMap = {
          1: "visit",
          2: "newest",
          3: "update",
        };
        const res = await exportRankExcel(typeMap[state.rankType] || "visit");
        const blob = res.data;
        const fileName = parseFileName(
          res.headers?.["content-disposition"] || "",
          `排行榜-${Date.now()}.xlsx`
        );
        triggerDownload(blob, fileName);
        ElMessage.success("排行榜导出成功");
      } finally {
        state.exporting = false;
      }
    };

    /**
     * 解析响应头中的下载文件名。
     */
    const parseFileName = (contentDisposition, fallbackName) => {
      const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
      if (utf8Match && utf8Match[1]) {
        return decodeURIComponent(utf8Match[1]);
      }
      const normalMatch = contentDisposition.match(/filename=([^;]+)/i);
      if (normalMatch && normalMatch[1]) {
        return normalMatch[1].replace(/"/g, "").trim();
      }
      return fallbackName;
    };

    /**
     * 触发浏览器下载。
     */
    const triggerDownload = (blob, fileName) => {
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement("a");
      link.style.display = "none";
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    };

    const wordCountFormat = (wordCount) => {
      const count = Number(wordCount || 0);
      if (count >= 10000) {
        return `${Math.floor(count / 10000)}万`;
      }
      if (count >= 1000) {
        return `${Math.floor(count / 1000)}千`;
      }
      return count;
    };

    return {
      ...toRefs(state),
      bookDetail,
      newestRank,
      visitRank,
      updateRank,
      downloadCurrentRank,
      wordCountFormat,
    };
  },
};
</script>

<style>
.el-pagination {
  justify-content: center;
}
.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background-color: #2196F3 !important;
}
.el-pagination {
  --el-pagination-hover-color: #2196F3 !important;
}
.rank-export-btn {
  color: #4b6f99;
  border: 1px solid #d5e4f6;
  border-radius: 16px;
  line-height: 28px;
  padding: 0 12px;
  margin-top: 4px;
}
.rank-export-btn:hover {
  color: #2f628f;
  border-color: #8bb4de;
}
</style>
