<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <div class="my_l">
        <ul class="log_list">
          <li>
            <router-link class="link_4 on" :to="{ name: 'authorBookUpload' }">上传小说</router-link>
          </li>
          <li>
            <router-link class="link_4" :to="{ name: 'authorUploadRecords' }">上传记录</router-link>
          </li>
        </ul>
      </div>
      <div class="my_r">
        <div class="my_bookshelf">
          <div class="title cf">
            <h2 class="fl">TXT上传小说</h2>
            <div class="fr">
              <router-link :to="{ name: 'authorUploadRecords' }" class="btn_red">查看上传记录</router-link>
            </div>
          </div>
          <div class="upload-form">
            <h3>小说基础信息填写</h3>
            <ul class="log_list">
              <b>作品方向：</b>
              <li>
                <select
                  v-model="book.workDirection"
                  class="s_input"
                  id="workDirection"
                  name="workDirection"
                  @change="loadCategoryList()"
                >
                  <option value="0">男频</option>
                  <option value="1">女频</option>
                </select>
              </li>
              <b>分类：</b>
              <li>
                <select class="s_input" id="catId" name="catId" v-model="book.categoryId" @change="categoryChange">
                  <option :value="item.id" v-for="(item, index) in bookCategorys" :key="index">
                    {{ item.name }}
                  </option>
                </select>
              </li>
              <b>小说名：</b>
              <li>
                <input v-model="book.bookName" type="text" id="bookName" name="bookName" class="s_input" />
              </li>
              <b>作者名：</b>
              <li>
                <input v-model="book.authorName" type="text" id="authorName" name="authorName" class="s_input" />
              </li>
              <b>小说封面：</b>
              <li style="position: relative">
                <el-upload
                  class="avatar-uploader"
                  :action="baseUrl + '/front/resource/image'"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <img :src="book.picUrl ? imgBaseUrl + book.picUrl : picUpload" class="avatar" />
                </el-upload>
              </li>
              <b>小说简介（可选）：</b>
              <li>
                <textarea v-model="book.bookDesc" name="bookDesc" rows="4" cols="53" id="bookDesc" class="textarea"></textarea>
                <p class="form-tip">不填时，系统将自动使用第一章前6行生成简介。</p>
              </li>
              <b>是否收费：</b>
              <li>
                <label class="radio-item"><input v-model="book.isVip" type="radio" value="0" /> 免费</label>
                <label class="radio-item"><input v-model="book.isVip" type="radio" value="1" /> 收费</label>
              </li>
              <b>TXT文件：</b>
              <li>
                <input type="file" accept=".txt,text/plain" @change="handleTxtChange" />
                <p class="form-tip">仅支持 TXT，建议 UTF-8 编码，最大 20MB。</p>
                <p class="txt-name" v-if="txtFileName">已选择：{{ txtFileName }}</p>
              </li>
              <li style="margin-top: 20px">
                <input type="button" @click="submitUpload" value="开始上传并拆分章节" class="btn_red" />
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { listCategorys } from "@/api/book";
import { getUploadPermission, uploadBookByTxt } from "@/api/author";
import AuthorHeader from "@/components/author/Header.vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorBookUpload",
  components: {
    AuthorHeader,
  },
  setup() {
    const router = useRouter();
    const state = reactive({
      book: {
        workDirection: 0,
        isVip: 0,
        authorName: "",
        bookDesc: "",
      },
      txtFile: null,
      txtFileName: "",
      bookCategorys: [],
      baseUrl: process.env.VUE_APP_BASE_API_URL,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(async () => {
      await loadCategoryList();
      await checkPermission();
    });

    /**
     * 加载小说分类。
     */
    const loadCategoryList = async () => {
      const { data } = await listCategorys({ workDirection: state.book.workDirection });
      if (!data || data.length === 0) {
        return;
      }
      state.book.categoryId = data[0].id;
      state.book.categoryName = data[0].name;
      state.bookCategorys = data;
    };

    /**
     * 校验上传权限，避免无权限用户继续操作。
     */
    const checkPermission = async () => {
      const { data } = await getUploadPermission();
      if (Number(data) !== 1) {
        ElMessage.error("当前账号暂无上传小说权限，请联系管理员开通。");
        router.push({ name: "authorBookList" });
      }
    };

    /**
     * 小说分类变更时同步分类名。
     */
    const categoryChange = (event) => {
      state.bookCategorys.forEach((category) => {
        if (String(category.id) === String(event.target.value)) {
          state.book.categoryName = category.name;
        }
      });
    };

    /**
     * 封面上传前校验。
     */
    const beforeAvatarUpload = (rawFile) => {
      if (rawFile.type !== "image/jpeg") {
        ElMessage.error("必须上传 JPG 格式的图片");
        return false;
      }
      if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error("图片大小最大 5MB");
        return false;
      }
      return true;
    };

    /**
     * 封面上传成功回调。
     */
    const handleAvatarSuccess = (response) => {
      state.book.picUrl = response.data;
    };

    /**
     * 选择 TXT 文件。
     */
    const handleTxtChange = (event) => {
      const file = event.target.files[0];
      state.txtFile = file || null;
      state.txtFileName = file ? file.name : "";
    };

    /**
     * 提交上传请求。
     */
    const submitUpload = async () => {
      if (!state.book.bookName) {
        ElMessage.error("书名不能为空");
        return;
      }
      if (!state.book.authorName) {
        ElMessage.error("作者名不能为空");
        return;
      }
      if (!state.book.picUrl) {
        ElMessage.error("封面不能为空");
        return;
      }
      if (!state.txtFile) {
        ElMessage.error("请先选择 TXT 文件");
        return;
      }
      const formData = new FormData();
      formData.append("workDirection", state.book.workDirection);
      formData.append("categoryId", state.book.categoryId);
      formData.append("categoryName", state.book.categoryName);
      formData.append("picUrl", state.book.picUrl);
      formData.append("bookName", state.book.bookName);
      formData.append("authorName", state.book.authorName);
      formData.append("bookDesc", state.book.bookDesc || "");
      formData.append("isVip", state.book.isVip);
      formData.append("file", state.txtFile);
      await uploadBookByTxt(formData);
      ElMessage.success("上传成功，已自动拆分章节。");
      router.push({ name: "authorUploadRecords" });
    };

    return {
      ...toRefs(state),
      picUpload,
      loadCategoryList,
      categoryChange,
      beforeAvatarUpload,
      handleAvatarSuccess,
      handleTxtChange,
      submitUpload,
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

.upload-form {
  width: 460px;
  padding: 10px 20px 30px;
  border-top: 1px solid #f1f1f1;
  animation: fadeInUp 0.35s ease;
}

.upload-form h3 {
  font-size: 20px;
  font-weight: normal;
  line-height: 1;
  text-align: left;
  margin: 14px 0 22px;
  color: #333;
}

.upload-form .s_input {
  width: 100%;
  margin-bottom: 18px;
  padding: 0 10px;
  border-color: #ddd;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.upload-form .s_input:focus {
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
  outline: none;
}

.upload-form b {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 600;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.radio-item {
  margin-right: 20px;
  color: #555;
}

.txt-name {
  margin-top: 8px;
  font-size: 13px;
  color: #2196f3;
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
