export interface QueryRequest{
    page: number;
    pageSize: number;
    filter?: string;
    sort?: QueryRequestSort;
    excludeSelf?: boolean;
}

export interface QueryRequestSort{
    sortType: QueryRequestSortType;
    sortField: string;
}

export enum QueryRequestSortType{
    ASC = "asc",
    DESC = "desc",
}

export interface QueryParams{
    page: number;
    size: number;
    filter?: string;
    sort?: string;
    excludeSelf?: boolean;
}
