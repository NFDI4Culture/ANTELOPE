export class EnvService {
    public static IS_DEV: boolean = process.env.NODE_ENV === "development";
}