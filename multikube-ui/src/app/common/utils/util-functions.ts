import {QueryParams, QueryRequest} from '../rest/types/requests/query-request';

export function resolveErrorMessage(error: any): string{
    return error?.error?.error || error?.message || error?.error?.message || error;
}

export function buildQueryParams(queryRequest: QueryRequest): QueryParams | any{
    const params: QueryParams = {
        page: queryRequest.page,
        size: queryRequest.pageSize,
    };

    if(queryRequest.excludeSelf){
        params.excludeSelf = queryRequest.excludeSelf;
    }

    if (queryRequest.filter) {
        params.filter = queryRequest.filter;
    }

    if (queryRequest.sort) {
        params.sort = `${queryRequest.sort.sortField},${queryRequest.sort.sortType.toLowerCase()}`;
    }

    return params;
}

export interface GridPropertyFilter {
    property: string;
    value: string;
}

export function buildRestGridFilter(gridFilters: GridPropertyFilter[] | any[] | undefined): string | undefined{
    if(gridFilters){
        let restFilter = "";
        gridFilters.forEach((gridFilter, index) => {
            restFilter += `${gridFilter.property}==${gridFilter.value}`;
            restFilter += (index != gridFilters.length - 1) ? "," : "";
        })

        return restFilter;
    }

    return undefined;
}
