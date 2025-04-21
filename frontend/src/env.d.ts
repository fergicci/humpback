
interface ImportMetaEnv {
    readonly VITE_APPLICATION_CONTACT_ADDRESSLINE_PT_BR: string;
    readonly VITE_APPLICATION_CONTACT_CITY_PT_BR: string;
    readonly VITE_APPLICATION_CONTACT_COUNTRY_PT_BR: string;
    readonly VITE_APPLICATION_CONTACT_PHONE_PT_BR: string;
    readonly VITE_APPLICATION_CONTACT_PERSON_PT_BR: string;
  
    readonly VITE_APPLICATION_CONTACT_ADDRESSLINE_EN: string;
    readonly VITE_APPLICATION_CONTACT_CITY_EN: string;
    readonly VITE_APPLICATION_CONTACT_COUNTRY_EN: string;
    readonly VITE_APPLICATION_CONTACT_PHONE_EN: string;
    readonly VITE_APPLICATION_CONTACT_PERSON_EN: string;
  }
  
  interface ImportMeta {
    readonly env: ImportMetaEnv;
  }