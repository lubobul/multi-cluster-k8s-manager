export interface PaginatedResponse<T> {
    content: T[]; // Array of items of type T
    totalElements: number; // Total number of elements
    totalPages: number; // Total number of pages
    currentPage: number; // Current page number
    pageSize: number; // Size of each page
    first: boolean; // Indicates if this is the first page
    last: boolean; // Indicates if this is the last page
}
