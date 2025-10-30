import { api, ApiResponse, PagedResponse } from "./api";

export interface NewsItem {
  id: string;
  title: string;
  content: string;
  createdAt: string;
}

export async function getNews(
  lang: string,
  page = 1,
  size = 10
): Promise<PagedResponse<NewsItem>> {
  try {
    const { data } = await api.get<ApiResponse<PagedResponse<NewsItem>>>(
      "/news", // baseURL already includes /api/v1
      {
        params: { page, size },
        headers: { "Accept-Language": lang },
      }
    );

    if (data?.success && data.data) {
      return data.data;
    }
  } catch (err) {
    console.error("Error fetching news:", err);
  }

  // Safe fallback
  return { content: [], page, size, totalElements: 0, totalPages: 0 };
}
