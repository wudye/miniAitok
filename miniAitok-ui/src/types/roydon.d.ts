// 精确声明 `@/utils/roydon` 模块的命名导出与默认导出，基于实现文件 src/utils/roydon.js
declare module '@/utils/roydon' {
  export function encodeData(str: string): string;
  export function decodeData(str: string): number | null;
  export function removeHtmlTags(str: string): string;
  export function formatAge(birthday: string): number[] | null;
  export function getAge(birthday: string): number | null;
  export function smartDateFormat(date: string | number | Date): string;
  export function parseTime(time?: any, pattern?: string): string | null;
  export function resetForm(refName: any): void;
  export function addDateRange(params: any, dateRange?: any[], propName?: string): any;
  export function sprintf(str: string, ...args: any[]): string;
  export function parseStrEmpty(str: any): string;
  export function mergeRecursive(source: any, target: any): any;
  export function handleTree(data: any[], id?: string, parentId?: string, children?: string): any[];
  export function tansParams(params: Record<string, any>): string;
  export function blobValidate(data: Blob | Response): Promise<boolean>;
  export const isMobile: () => boolean;

  const roydon: {
    encodeData: typeof encodeData;
    decodeData: typeof decodeData;
    removeHtmlTags: typeof removeHtmlTags;
    formatAge: typeof formatAge;
    getAge: typeof getAge;
    smartDateFormat: typeof smartDateFormat;
    parseTime: typeof parseTime;
    resetForm: typeof resetForm;
    addDateRange: typeof addDateRange;
    sprintf: typeof sprintf;
    parseStrEmpty: typeof parseStrEmpty;
    mergeRecursive: typeof mergeRecursive;
    handleTree: typeof handleTree;
    tansParams: typeof tansParams;
    blobValidate: typeof blobValidate;
    isMobile: typeof isMobile;
  };

  export default roydon;
}

// 同时声明带 .js 扩展名的导入形式，以防代码使用 '@/utils/roydon.js'
declare module '@/utils/roydon.js' {
  export * from '@/utils/roydon';
  import roydon from '@/utils/roydon';
  export default roydon;
}

// 通配声明以覆盖可能的相对/不同别名路径
declare module '*/roydon' {
  export * from '@/utils/roydon';
  import roydon from '@/utils/roydon';
  export default roydon;
}
