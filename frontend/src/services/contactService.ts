import axios, { AxiosError } from "axios";

export interface ContactRequest {
  name: string;
  email: string;
  telephone: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  code: number;
  message: string;
  details: string[];
}

export const sendContactMessage = async (data: ContactRequest): Promise<void> => {
  try {
    await axios.post("/api/v1/contacts", data);
  } catch (error) {
    const err = error as AxiosError<{ error: ApiError }>;
    if (err.response?.data?.error) {
      throw err.response.data.error;
    }
    throw error;
  }
};