declare module '@/api/member' {
  export function login(username: string, password: string): Promise<any>
  export const userLogin: (username: string, password: string) => Promise<any>
  export const userSmsLogin: (telephone: string, smsCode: string) => Promise<any>
  export function register(data: any): Promise<any>
  export function getInfo(): Promise<any>
  export function getPersonInfo(userId: string | number): Promise<any>
  export function logout(): Promise<any>
  export function updateUserProfile(data: any): Promise<any>
  export function avatar(file: File | Blob): Promise<any>
  export function updateMemberInfo(data: any): Promise<any>
  const _default: any
  export default _default
}
