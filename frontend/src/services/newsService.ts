import axios from "axios";

export interface NewsItem {
  id: string;
  title: string;
  content: string;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export async function getNews(lang: string, page = 1, size = 10): Promise<NewsItem[]> {
  try {
    const response = await axios.get<ApiResponse<PagedResponse<NewsItem>>>(`/api/v1/news`, {
      params: { lang, page, size },
    });

    if (response.data.success) {
      return response.data.data.content || [];
    } else {
      console.error("Failed to fetch news: success=false");
      return [];
    }
  } catch (error) {
    console.error("Error fetching news:", error);
    return [];
  }
}